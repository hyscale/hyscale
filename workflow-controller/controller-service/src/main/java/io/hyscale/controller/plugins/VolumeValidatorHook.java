package io.hyscale.controller.plugins;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.hyscale.commons.component.InvokerHook;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.utils.HyscaleStringUtil;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.handler.impl.V1PersistentVolumeClaimHandler;
import io.hyscale.deployer.services.handler.impl.V1PodHandler;
import io.hyscale.deployer.services.handler.impl.V1StorageClassHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.model.service.Volume;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1ResourceRequirements;
import io.kubernetes.client.models.V1StorageClass;

/**
 * Validate Volumes:
 * Storage Class should be valid - Exception if invalid
 * Size and Storage class modification - Warn if changed
 *
 * @author tushart
 */
@Component
public class VolumeValidatorHook implements InvokerHook<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(VolumeValidatorHook.class);

    private static final String EXISTING = "Existing";

    private static final String STORAGE = "storage";

    private static final String STORAGE_CLASS_FIELD = "StorageClass";

    private static final String SIZE_FIELD = "size";

    private static final String IGNORED = "Ignored";

    private static final String from = "from ";

    private static final String to = "to ";

    @Autowired
    private K8sClientProvider clientProvider;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    private List<V1StorageClass> storageClassList = new ArrayList<>();

    @Override
    public void preHook(WorkflowContext context) throws HyscaleException {
        logger.debug("Validating volumes from the service spec");
        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            logger.debug("Service spec not found");
        }
        TypeReference<List<Volume>> volTypeRef = new TypeReference<List<Volume>>() {
        };
        List<Volume> volumeList = serviceSpec.get(HyscaleSpecFields.volumes, volTypeRef);

        if (volumeList == null || volumeList.isEmpty()) {
            logger.debug("No volumes found for validation");
            return;
        }
        ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfigBuilder.getAuthConfig());
        initStorageClass(apiClient);

        // Validate Storage class
        validateStorageClass(volumeList);

        logger.debug("Storage class provided are valid");

        // Validate volume edit
        validateVolumeEdit(apiClient, context, volumeList);

    }

    @Override
    public void postHook(WorkflowContext context) throws HyscaleException {

    }

    @Override
    public void onError(WorkflowContext context, Throwable th) {
        logger.error("Error while validating Volumes");
        context.setFailed(true);
        if (th instanceof HyscaleException) {
            context.setHyscaleException((HyscaleException) th);
        }
    }

    /**
     * Validate Storage class
     * Checks for the following conditions in the cluster and fails when
     * <ol>
     * <li>There are no storage classes</li>
     * <li>No storage class is defined & default storage class does not exist in the cluster</li>
     * <li>Provided storage class does not exist in cluster</li>
     * </ol>
     *
     * @param volumeList
     * @throws HyscaleException
     */
    private void validateStorageClass(List<Volume> volumeList) throws HyscaleException {
        String defaultStorageClass = getDefaultStorageClass();
        Set<String> storageClassAllowed = storageClassList.stream().map(each -> each.getMetadata().getName())
                .collect(Collectors.toSet());
        logger.debug("Allowed Storage classes are : {}", storageClassAllowed);
        boolean isFailed = false;
        DeployerErrorCodes errorCode = null;
        StringBuilder failMsgBuilder = new StringBuilder();
        for (Volume volume : volumeList) {
            String storageClass = volume.getStorageClass();
            if (storageClass == null && StringUtils.isBlank(defaultStorageClass)) {
                isFailed = true;
                errorCode = DeployerErrorCodes.MISSING_DEFAULT_STORAGE_CLASS;
                failMsgBuilder.append(volume.getName());
                failMsgBuilder.append(ToolConstants.COMMA);
            }
            if (storageClass != null && !storageClassAllowed.contains(storageClass)) {
                isFailed = true;
                errorCode = DeployerErrorCodes.INVALID_STORAGE_CLASS_FOR_VOLUME;
                failMsgBuilder.append(storageClass);
                failMsgBuilder.append(ToolConstants.COMMA);
            }
        }
        if (isFailed) {
            String failMsg = HyscaleStringUtil.removeSuffixStr(failMsgBuilder, ToolConstants.COMMA);
            throw new HyscaleException(errorCode, failMsg, storageClassAllowed.toString());
        }

    }

    /**
     * Fetch Pods and PVC based on selector
     * For each volume and each pod name
     * Check if PVC size or storage class is modified
     * If modified persist warn message
     * Warn msg Format: <VolumeName>::Existing<value>:Ignored<value>
     *
     * @param apiClient
     * @param volumeList
     */
    private void validateVolumeEdit(ApiClient apiClient, WorkflowContext context, List<Volume> volumeList) {
        ResourceLifeCycleHandler resourceHandler = ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        if (resourceHandler == null) {
            return;
        }
        V1PodHandler podHandler = (V1PodHandler) resourceHandler;
        String appName = context.getAppName();
        String envName = context.getEnvName();
        String serviceName = context.getServiceName();
        String namespace = context.getNamespace();

        String selector = ResourceSelectorUtil.getSelector(appName, envName, serviceName);
        List<V1Pod> podList = null;
        try {
            podList = podHandler.getBySelector(apiClient, selector, true, namespace);
        } catch (HyscaleException e) {
            logger.debug(
                    "Failed to get pods for App: {}, Environment: {}, Service: {}, ignoring volume update validation",
                    appName, envName, serviceName);
            return;
        }
        if (podList == null || podList.isEmpty()) {
            logger.debug("No pods found for App: {}, Environment: {}, Service: {}, ignoring volume update validation",
                    appName, envName, serviceName);
        }
        resourceHandler = ResourceHandlers.getHandlerOf(ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());
        if (resourceHandler == null) {
            return;
        }
        V1PersistentVolumeClaimHandler pvcHandler = (V1PersistentVolumeClaimHandler) resourceHandler;
        StringBuilder warnMsgBuilder = new StringBuilder();
        for (Volume volume : volumeList) {
            Set<String> pvcNames = podList.stream()
                    .map(each -> volume.getName() + ToolConstants.DASH + each.getMetadata().getName())
                    .collect(Collectors.toSet());

            pvcNames.stream().forEach(pvcName -> {
                V1PersistentVolumeClaim pvc = null;
                try {
                    pvc = pvcHandler.get(apiClient, pvcName, namespace);
                } catch (HyscaleException ex) {
                    logger.debug("Error while fetching PVC: {}, error: {}", pvcName, ex.getMessage());
                }
                if (pvc == null) {
                    logger.debug("PVC: {} not found", pvcName);
                    return;
                }
                String storageClass = pvc.getSpec().getStorageClassName();
                if (StringUtils.isBlank(storageClass)) {
                    logger.debug("Storage class not found in spec, getting from annotation");
                    storageClass = pvc.getMetadata().getAnnotations()
                            .get(AnnotationKey.K8S_STORAGE_CLASS.getAnnotation());
                }
                Quantity existingSize = pvc.getStatus().getCapacity() != null
                        ? pvc.getStatus().getCapacity().get(STORAGE)
                        : null;
                if (existingSize == null) {
                    logger.debug("Size not found in status, getting from spec");
                    V1ResourceRequirements resources = pvc.getSpec().getResources();
                    existingSize = resources != null
                            ? (resources.getRequests() != null ? resources.getRequests().get(STORAGE) : null)
                            : null;
                }
                Quantity newSize = Quantity.fromString(StringUtils.isNotBlank(volume.getSize()) ? volume.getSize()
                        : K8SRuntimeConstants.DEFAULT_VOLUME_SIZE);
                boolean isStorageClassSame = matchStorageClass(storageClass, volume.getStorageClass());
                boolean isSizeSame = newSize.equals(existingSize);
                if (!isStorageClassSame || !isSizeSame) {
                    if (!isStorageClassSame) {
                        warnMsgBuilder.append(getWarnMsg(STORAGE_CLASS_FIELD, storageClass, volume.getStorageClass() != null ? volume.getStorageClass() : getDefaultStorageClass()));
                    }
                    if (!isSizeSame) {
                        warnMsgBuilder.append(
                                getWarnMsg(SIZE_FIELD, existingSize.toSuffixedString(), newSize.toSuffixedString()));
                    }
                }
            });
        }
        String warnMsg = warnMsgBuilder.toString();
        if (StringUtils.isNotBlank(warnMsg)) {
            warnMsg = HyscaleStringUtil.removeSuffixStr(warnMsg, ToolConstants.COMMA);
            logger.debug(DeployerActivity.IGNORING_VOLUME_MODIFICATION.getActivityMessage(), warnMsg);
            WorkflowLogger.persist(DeployerActivity.IGNORING_VOLUME_MODIFICATION, warnMsg);
        }
    }

    /**
     * @param field
     * @param existingObj
     * @param newObj
     * @return String field:Existing:existingObj,New:newObj
     * // field has been changed from {} to {}
     */
    private Object getWarnMsg(String field, Object existingObj, Object newObj) {

        StringBuilder sb = new StringBuilder();
        sb.append(field).append(ToolConstants.SPACE);
        sb.append("has been changed").append(ToolConstants.SPACE);
        sb.append(from).append(existingObj).append(ToolConstants.SPACE);
        sb.append(to).append(newObj);
        return sb.toString();
    }


    private Predicate<V1StorageClass> isDefaultStorageClass() {
        return v1StorageClass -> {
            Map<String, String> annotations = v1StorageClass.getMetadata().getAnnotations();
            if (annotations != null) {
                String annotationValue = annotations.get(AnnotationKey.DEFAULT_STORAGE_CLASS.getAnnotation());
                if (StringUtils.isNotBlank(annotationValue)) {
                    return Boolean.valueOf(annotationValue);
                }
            }
            return false;
        };
    }

    private String getDefaultStorageClass() {
        if (storageClassList != null && !storageClassList.isEmpty()) {
            for (V1StorageClass each : storageClassList) {
                Map<String, String> annotations = each.getMetadata().getAnnotations();
                if (annotations != null) {
                    String annotationValue = annotations.get(AnnotationKey.DEFAULT_STORAGE_CLASS.getAnnotation());
                    if (StringUtils.isNotBlank(annotationValue) && Boolean.valueOf(annotationValue)) {
                        return each.getMetadata().getName();
                    }
                }
            }
        }
        return null;
    }

    private void initStorageClass(ApiClient apiClient) throws HyscaleException {
        ResourceLifeCycleHandler resourceHandler = ResourceHandlers.getHandlerOf(ResourceKind.STORAGE_CLASS.getKind());
        if (resourceHandler == null) {
            return;
        }
        V1StorageClassHandler storageClassHandler = (V1StorageClassHandler) resourceHandler;

        // Storage class are cluster based no need of selector and namespace
        try {
            storageClassList = storageClassHandler.getAll(apiClient);
        } catch (HyscaleException ex) {
            logger.error("Error while getting storage class list, error {}", ex.getMessage());
            throw new HyscaleException(DeployerErrorCodes.NO_STORAGE_CLASS_IN_K8S);
        }

        if (storageClassList == null || storageClassList.isEmpty()) {
            throw new HyscaleException(DeployerErrorCodes.NO_STORAGE_CLASS_IN_K8S);
        }
    }

    private boolean matchStorageClass(String existing, String modified) {
        if (StringUtils.isBlank(modified)) {
            return (existing != null && getDefaultStorageClass() != null) ? existing.equals(getDefaultStorageClass()) : false;
        }
        if (StringUtils.isNotBlank(existing) && StringUtils.isNotBlank(modified)) {
            return existing.equals(modified);
        }
        return false;
    }
}
