package io.hyscale.ctl.controller.plugins;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.ctl.commons.component.ComponentInvokerPlugin;
import io.hyscale.ctl.commons.constants.K8SRuntimeConstants;
import io.hyscale.ctl.commons.constants.ToolConstants;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.AnnotationKey;
import io.hyscale.ctl.commons.models.K8sAuthorisation;
import io.hyscale.ctl.commons.utils.HyscaleStringUtil;
import io.hyscale.ctl.commons.utils.ResourceSelectorUtil;
import io.hyscale.ctl.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.deployer.core.model.ResourceKind;
import io.hyscale.ctl.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.ctl.deployer.services.handler.ResourceHandlers;
import io.hyscale.ctl.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.ctl.deployer.services.handler.impl.V1PersistentVolumeClaimHandler;
import io.hyscale.ctl.deployer.services.handler.impl.V1PodHandler;
import io.hyscale.ctl.deployer.services.handler.impl.V1StorageClassHandler;
import io.hyscale.ctl.deployer.services.model.DeployerActivity;
import io.hyscale.ctl.deployer.services.provider.K8sClientProvider;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.servicespec.commons.model.service.Volume;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1ResourceRequirements;
import io.kubernetes.client.models.V1StorageClass;

/**
 * Validates Volume:
 * Storage Class should be valid - Exception if invalid
 * Size and Storage class modification - Warn if changed
 * @author tushart
 *
 */
@Component
public class VolumeValidatorPlugin implements ComponentInvokerPlugin<WorkflowContext> {

	private static final Logger logger = LoggerFactory.getLogger(VolumeValidatorPlugin.class);

	private static final String EXISTING = "Existing";

	private static final String STORAGE = "storage";

	private static final String STORAGE_CLASS_FIELD = "Storage Class";

	private static final String SIZE_FIELD = "size";

	private static final String IGNORED = "Ignored";

	@Autowired
	private K8sClientProvider clientProvider;

	@Autowired
	private K8sAuthConfigBuilder authConfigBuilder;

	@Override
	public void doBefore(WorkflowContext context) throws HyscaleException {
		logger.debug("Starting volume validation");
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

		// Validate Storage class
		validateStorageClass(apiClient, volumeList);

		logger.debug("Storage class provided are valid");

		// Validate volume edit
		validateVolumeEdit(apiClient, context, volumeList);

	}

	@Override
	public void doAfter(WorkflowContext context) throws HyscaleException {

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
	 * Empty storage class - not allowed
	 * Provided storage class should exist in cluster
	 * @param apiClient
	 * @param volumeList
	 * @throws HyscaleException
	 */
	private void validateStorageClass(ApiClient apiClient, List<Volume> volumeList) throws HyscaleException {
		ResourceLifeCycleHandler resourceHandler = ResourceHandlers.getHandlerOf(ResourceKind.STORAGE_CLASS.getKind());
		if (resourceHandler == null) {
			return;
		}
		V1StorageClassHandler storageClassHandler = (V1StorageClassHandler) resourceHandler;

		List<V1StorageClass> storageClassList = null;
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

		Set<String> storageClassAllowed = storageClassList.stream().map(each -> each.getMetadata().getName())
				.collect(Collectors.toSet());
		logger.debug("Storage classes allowed: {}", storageClassAllowed);
		boolean isFailed = false;
		StringBuilder failMsgBuilder = new StringBuilder();
		for (Volume volume : volumeList) {
			String storageClass = volume.getStorageClass();
			if (!storageClassAllowed.contains(storageClass)) {
				isFailed = true;
				failMsgBuilder.append(volume.getName());
				failMsgBuilder.append(ToolConstants.COLON);
				failMsgBuilder.append(storageClass);
				failMsgBuilder.append(ToolConstants.COMMA);
			}
		}
		if (isFailed) {
			String failMsg = HyscaleStringUtil.removeSuffixStr(failMsgBuilder, ToolConstants.COMMA);
			logger.error("Storage class in volume invalid, {}", failMsg);
			throw new HyscaleException(DeployerErrorCodes.INVALID_STORAGE_CLASS_FOR_VOLUME, failMsg);
		}

	}

	/**
	 * Fetch Pods and PVC based on selector
	 * For each volume and each pod name
	 * 		Check if PVC size or storage class is modified
	 * 		If modified persist warn message
	 * Warn msg Format: <VolumeName>::Existing<value>:Ignored<value>
	 * @param apiClient
	 * @param serviceName 
	 * @param appName 
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
				boolean isStorageClassSame = volume.getStorageClass().equalsIgnoreCase(storageClass);
				boolean isSizeSame = newSize.equals(existingSize);
				if (!isStorageClassSame || !isSizeSame) {
					warnMsgBuilder.append(volume.getName()).append(ToolConstants.DOUBLE_COLON);
					if (!isStorageClassSame) {
						warnMsgBuilder.append(getWarnMsg(STORAGE_CLASS_FIELD, storageClass, volume.getStorageClass()));
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
	 * 
	 * @param field
	 * @param existingObj
	 * @param newObj
	 * @return String field:Existing:existingObj,New:newObj,
	 */
	private Object getWarnMsg(String field, Object existingObj, Object newObj) {
		StringBuilder sb = new StringBuilder();
		sb.append(field);
		sb.append(ToolConstants.COLON);
		sb.append(IGNORED);
		sb.append(ToolConstants.COLON);
		sb.append(newObj);
		sb.append(ToolConstants.COMMA);
		sb.append(EXISTING);
		sb.append(ToolConstants.COLON);
		sb.append(existingObj);
		sb.append(ToolConstants.COMMA);

		return sb.toString();
	}

}
