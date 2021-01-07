/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.deployer.services.handler.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.kubernetes.client.openapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.ActivityContext;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.ResourceLabelKey;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.GsonProviderUtil;
import io.hyscale.commons.utils.ResourceLabelUtil;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.core.model.ResourceOperation;
import io.hyscale.deployer.services.constants.DeployerConstants;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.PodParentHandler;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.model.PodCondition;
import io.hyscale.deployer.services.model.ResourceStatus;
import io.hyscale.deployer.services.util.ExceptionHelper;
import io.hyscale.deployer.services.util.K8sPodUtil;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;

/**
 * @author tushart
 */

public class V1StatefulSetHandler extends PodParentHandler<V1StatefulSet> implements ResourceLifeCycleHandler<V1StatefulSet> {
    private static final Logger LOGGER = LoggerFactory.getLogger(V1StatefulSetHandler.class);

    @Override
    public V1StatefulSet create(ApiClient apiClient, V1StatefulSet resource, String namespace) throws HyscaleException {
        if (resource == null) {
            LOGGER.debug("Cannot create null StatefulSet");
            return resource;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_STATEFULSET);
        AppsV1Api appsV1Api = new AppsV1Api(apiClient);
        String name = resource.getMetadata().getName();
        V1StatefulSet statefulSet = null;
        try {
            resource.getMetadata().putAnnotationsItem(
                    AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(), GsonProviderUtil.getPrettyGsonBuilder().toJson(resource));
            statefulSet = appsV1Api.createNamespacedStatefulSet(namespace, resource, DeployerConstants.TRUE, null, null);
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.CREATE));
            LOGGER.error("Error while creating statefulset {} in namespace {}, error {}", name, namespace,
                    ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        LOGGER.info("Created Statefulset {} in namespace {}",name,namespace);
        WorkflowLogger.endActivity(Status.DONE);
        return statefulSet;
    }

    @Override
    public boolean update(ApiClient apiClient, V1StatefulSet resource, String namespace) throws HyscaleException {
        if (resource == null) {
            LOGGER.debug("Cannot update null StatefulSet");
            return false;
        }
        AppsV1Api appsV1Api = new AppsV1Api(apiClient);
        String name = resource.getMetadata().getName();
        V1StatefulSet existingStatefulSet = null;
        try {
            existingStatefulSet = get(apiClient, name, namespace);
        } catch (HyscaleException ex) {
            LOGGER.debug("Error while getting StatefulSet {} in namespace {} for Update, creating new", name,
                    namespace);
            V1StatefulSet statefulSet = create(apiClient, resource, namespace);
            return statefulSet != null;
        }

        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_STATEFULSET);
        try {
            String resourceVersion = existingStatefulSet.getMetadata().getResourceVersion();
            resource.getMetadata().setResourceVersion(resourceVersion);
            appsV1Api.replaceNamespacedStatefulSet(name, namespace, resource, DeployerConstants.TRUE, null, null);
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_UPDATE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.UPDATE));
            LOGGER.error("Error while updating SatefulSet {} in namespace {}, error {}", name, namespace,
                    ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        LOGGER.info("Updated Statefulset {} in namespace {}",name,namespace);
        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

    @Override
    public V1StatefulSet get(ApiClient apiClient, String name, String namespace) throws HyscaleException {
        AppsV1Api appsV1Api = new AppsV1Api(apiClient);
        V1StatefulSet v1StatefulSet = null;
        try {
            v1StatefulSet = appsV1Api.readNamespacedStatefulSet(name, namespace, DeployerConstants.TRUE, null, null);
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET);
            LOGGER.error("Error while fetching StatefulSet {} in namespace {}, error {}", name, namespace,
                    ex.toString());
            throw ex;
        }
        return v1StatefulSet;
    }

    @Override
    public List<V1StatefulSet> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace) throws HyscaleException {
        AppsV1Api appsV1Api = new AppsV1Api(apiClient);
        String labelSelector = label ? selector : null;
        String fieldSelector = label ? null : selector;
        List<V1StatefulSet> statefulSets = null;
        try {
            V1StatefulSetList statefulSetList = appsV1Api.listNamespacedStatefulSet(namespace, DeployerConstants.TRUE, null,
                    null, fieldSelector, labelSelector, null, null, null, null);
            statefulSets = statefulSetList != null ? statefulSetList.getItems() : null;
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_BY_SELECTOR);
            LOGGER.error("Error while listing StatefulSets in namespace {}, with selectors {} , error {}", namespace,
                    selector, ex.toString());
            throw ex;
        }
        return statefulSets;
    }
    
    @Override
    public List<V1StatefulSet> listForAllNamespaces(ApiClient apiClient, String selector, boolean label)
            throws HyscaleException {
        AppsV1Api appsV1Api = new AppsV1Api(apiClient);
        String labelSelector = label ? selector : null;
        String fieldSelector = label ? null : selector;
        List<V1StatefulSet> statefulSets = null;
        try {
            V1StatefulSetList statefulSetList = appsV1Api.listStatefulSetForAllNamespaces(null, null, fieldSelector,
                    labelSelector, null, DeployerConstants.TRUE, null, null, null);
            statefulSets = statefulSetList != null ? statefulSetList.getItems() : null;
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_ALL);
            LOGGER.error("Error while listing StatefulSets all namespaces, with selectors {} , error {}", selector,
                    ex.toString());
            throw ex;
        }
        return statefulSets;
    }

    @Override
    public boolean patch(ApiClient apiClient, String name, String namespace, V1StatefulSet target) throws HyscaleException {
        if (target == null) {
            LOGGER.debug("Cannot patch null StatefulSet");
            return false;
        }
        AppsV1Api appsV1Api = new AppsV1Api(apiClient);
        target.getMetadata().putAnnotationsItem(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                GsonProviderUtil.getPrettyGsonBuilder().toJson(target));
        V1StatefulSet sourceStatefulSet = null;
        try {
            sourceStatefulSet = get(apiClient, name, namespace);
        } catch (HyscaleException e) {
            LOGGER.debug("Error while getting StatefulSet {} in namespace {} for Patch, creating new", name, namespace);
            V1StatefulSet statefulSet = create(apiClient, target, namespace);
            return statefulSet != null;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_STATEFULSET);
        Object patchObject = null;
        String lastAppliedConfig = sourceStatefulSet.getMetadata().getAnnotations()
                .get(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation());
        boolean deleteRequired = false;
        String serviceName = sourceStatefulSet.getMetadata().getLabels().get(ResourceLabelKey.SERVICE_NAME.getLabel());
        try {
            patchObject = K8sResourcePatchUtil.getJsonPatch(GsonProviderUtil.getPrettyGsonBuilder().fromJson(lastAppliedConfig, V1StatefulSet.class),
                    target, V1StatefulSet.class);
            deleteRequired = isDeletePodRequired(apiClient, serviceName, namespace);
            LOGGER.debug("Deleting existing pods for updating StatefulSet patch required :{}", deleteRequired);
            V1Patch v1Patch = new V1Patch(patchObject.toString());
            appsV1Api.patchNamespacedStatefulSet(name, namespace, v1Patch, DeployerConstants.TRUE, null, null, null);
        } catch (HyscaleException ex) {
            LOGGER.error("Error while creating patch for StatefulSet {}, source {}, target {}, error {}", name,
                    sourceStatefulSet, target, ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_PATCH_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.PATCH));
            LOGGER.error("Error while patching StatefulSet {} in namespace {} , error {}", name, namespace,
                    ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        } finally {
            if (deleteRequired) {
                V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
                podHandler.deleteBySelector(apiClient, getPodSelector(serviceName), true, namespace, false);
            }
        }
        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

    @Override
    public boolean delete(ApiClient apiClient, String name, String namespace, boolean wait) throws HyscaleException {
        ActivityContext activityContext = new ActivityContext(DeployerActivity.DELETING_STATEFULSET);
        WorkflowLogger.startActivity(activityContext);
        try {
            delete(apiClient, name, namespace);
            if (wait) {
                List<String> pendingStatefulSets = Lists.newArrayList();
                pendingStatefulSets.add(name);
                waitForResourceDeletion(apiClient, pendingStatefulSets, namespace, activityContext);
            }
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                WorkflowLogger.endActivity(activityContext, Status.NOT_FOUND);
                return false;
            }
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.DELETE));
            LOGGER.error("Error while deleting StatefulSet {} in namespace {} , error {}", name, namespace,
                    ex.toString());
            WorkflowLogger.endActivity(activityContext, Status.FAILED);
            throw ex;
        }
        LOGGER.info("Deleted StatefulSet {} in namespace {} ",name, namespace);
        WorkflowLogger.endActivity(activityContext, Status.DONE);
        return true;
    }

    private void delete(ApiClient apiClient, String name, String namespace) throws ApiException {
        AppsV1Api appsV1Api = new AppsV1Api(apiClient);
        V1DeleteOptions deleteOptions = getDeleteOptions();
        deleteOptions.setApiVersion("apps/v1");
        try {
            appsV1Api.deleteNamespacedStatefulSet(name, namespace, DeployerConstants.TRUE, null, null, null, null,
                    deleteOptions);
        } catch (JsonSyntaxException e) {
            // K8s end exception ignore
        }
    }

    @Override
    public boolean deleteBySelector(ApiClient apiClient, String selector, boolean label, String namespace, boolean wait) throws HyscaleException {
        try {
            List<V1StatefulSet> statefulSets = getBySelector(apiClient, selector, label, namespace);

            if (statefulSets == null || statefulSets.isEmpty()) {
                return false;
            }
            for (V1StatefulSet statefulSet : statefulSets) {
                delete(apiClient, statefulSet.getMetadata().getName(), namespace, wait);
            }
        } catch (HyscaleException e) {
            if (DeployerErrorCodes.RESOURCE_NOT_FOUND.equals(e.getHyscaleError())) {
                return false;
            }
            throw e;
        }
        return true;
    }

    @Override
    public String getKind() {
        return ResourceKind.STATEFUL_SET.getKind();
    }

    @Override
    public boolean cleanUp() {
        return true;
    }

    private boolean isDeletePodRequired(ApiClient apiClient, String name, String namespace) throws HyscaleException {
        V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        List<V1Pod> v1PodList = podHandler.getBySelector(apiClient, getPodSelector(name), true, namespace);
        boolean isPodInErrorState = false;
        if (v1PodList != null && !v1PodList.isEmpty()) {
            isPodInErrorState = v1PodList.stream()
                    .anyMatch(each -> !K8sPodUtil.getAggregatedStatusOfContainersForPod(each)
                            .equalsIgnoreCase(K8SRuntimeConstants.POD_RUNING_STATE_CONDITION)
                            || !K8sPodUtil.checkForPodCondition(each, PodCondition.READY));
        }
        return isPodInErrorState;
    }

    private String getPodSelector(String serviceName) {
        return ResourceSelectorUtil.getServiceSelector(null, serviceName);
    }

    @Override
    public ResourceStatus status(V1StatefulSet statefulSet) {
        V1StatefulSetStatus stsStatus = statefulSet.getStatus();
        if (stsStatus == null) {
            return ResourceStatus.FAILED;
        }
        String currentRevision = stsStatus.getCurrentRevision();
        String updateRevision = stsStatus.getUpdateRevision();
        // stsStatus.getConditions()
        Integer currentReplicas = stsStatus.getCurrentReplicas();
        Integer readyReplicas = stsStatus.getReadyReplicas();
        Integer intendedReplicas = statefulSet.getSpec().getReplicas();
        // Success case update remaining pods status and return
        if (updateRevision != null && updateRevision.equals(currentRevision) && intendedReplicas != null
                && intendedReplicas.equals(currentReplicas) && intendedReplicas.equals(readyReplicas)) {
            return ResourceStatus.STABLE;
        }

        if ((intendedReplicas == 0 && readyReplicas == null)) {
            return ResourceStatus.STABLE;
        }
        return ResourceStatus.PENDING;
    }

    @Override
    public int getWeight() {
        return ResourceKind.STATEFUL_SET.getWeight();
    }

    public List<String> getServiceNames(ApiClient apiClient, String selector, boolean label, String namespace)
            throws HyscaleException {
        return getServiceNames(getBySelector(apiClient, selector, label, namespace));
    }

    /**
     * @param statefulSetList
     * @return list of service names from label of statefulset
     */
    public List<String> getServiceNames(List<V1StatefulSet> statefulSetList) {
        if (statefulSetList == null) {
            return Collections.emptyList();
        }
        return statefulSetList.stream().filter(each -> each != null && each.getMetadata() != null)
                .map(each -> ResourceLabelUtil.getServiceName(each.getMetadata().getLabels()))
                .collect(Collectors.toList());
    }

    @Override
    public List<DeploymentStatus> getStatus(ApiClient apiClient, String selector, boolean label, String namespace) {
        try {
            return buildStatus(getBySelector(apiClient, selector, label, namespace));
        } catch (HyscaleException e) {
            LOGGER.error("Error while fetching StatefulSet with selector {} in namespace {}, error {}", selector,
                    namespace, e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public DeploymentStatus buildStatus(V1StatefulSet statefulSet) {
        if (statefulSet == null) {
            return null;
        }
        ResourceStatus resourceStatus = status(statefulSet);
        DeploymentStatus.ServiceStatus serviceStatus = ResourceStatus.getServiceStatus(resourceStatus);
        if (resourceStatus.equals(ResourceStatus.PENDING)) {
            if (statefulSet.getSpec().getReplicas() <= statefulSet.getStatus().getReadyReplicas()){
                serviceStatus = DeploymentStatus.ServiceStatus.SCALING_DOWN;
            }
        }
        if (statefulSet.getSpec().getReplicas() == 0 && resourceStatus.equals(ResourceStatus.STABLE)){
            serviceStatus = DeploymentStatus.ServiceStatus.NOT_RUNNING;
        }
        return buildStatusFromMetadata(statefulSet.getMetadata(), serviceStatus);
    }

    @Override
    public List<DeploymentStatus> buildStatus(List<V1StatefulSet> statefulSetList) {
        if (statefulSetList == null) {
            return Collections.emptyList();
        }
        List<DeploymentStatus> statuses = new ArrayList<>();
        statefulSetList.stream().forEach(each -> {
            DeploymentStatus deployStatus = buildStatus(each);
            if (deployStatus != null) {
                statuses.add(deployStatus);
            }
        });
        return statuses;
    }

    @Override
    protected String getPodRevision(ApiClient apiClient, String selector, boolean label, String namespace) {
        List<V1StatefulSet> statefulSetList = null;
        try {
            statefulSetList = getBySelector(apiClient, selector, label, namespace);
        } catch (HyscaleException e) {
            LOGGER.error("Error fetching deployment for pod selection, ignoring", e);
            return null;
        }
        if (statefulSetList == null || statefulSetList.isEmpty()) {
            return null;
        }
        V1StatefulSet v1StatefulSet = statefulSetList.get(0);
        return getPodRevision(null, v1StatefulSet);
    }

    public String getControllerRevisoionHash(V1StatefulSet v1StatefulSet) {
        if (v1StatefulSet == null) {
            return null;
        }

        Map<String, String> annotations = v1StatefulSet.getMetadata().getAnnotations();
        if (annotations == null || annotations.isEmpty()) {
            return null;
        }
        return annotations.get(K8SRuntimeConstants.K8S_STS_CONTROLLER_REVISION_HASH);
    }

    /**
     * @param apiClient
     * @param v1StatefulSet
     * @return It will return revision of pod
     */

    @Override
    protected String getPodRevision(ApiClient apiClient, V1StatefulSet v1StatefulSet) {
        if (v1StatefulSet == null) {
            return null;
        }
        V1StatefulSetStatus stsStatus = v1StatefulSet.getStatus();
        if (stsStatus == null) {
            return null;
        }
        String currentRevision = stsStatus.getCurrentRevision();
        String updateRevision = stsStatus.getUpdateRevision();
        LOGGER.debug("Current Revision: {}", currentRevision);
        LOGGER.debug("Updated Revision: {}", updateRevision);
        return K8SRuntimeConstants.K8S_STS_CONTROLLER_REVISION_HASH + "=" + updateRevision;
    }

    /**
     * @return It will return replica of pod, if replica is not there then it will
     * return default value 1
     */

    @Override
    public Integer getReplicas(V1StatefulSet t) {
        return t != null ? t.getSpec().getReplicas() : K8SRuntimeConstants.DEFAULT_REPLICA_COUNT;
    }

    @Override
    public boolean scale(ApiClient apiClient, V1StatefulSet v1StatefulSet, String namespace, int value) throws HyscaleException {
        if (v1StatefulSet == null) {
            LOGGER.error("Cannot scale 'null' deployment");
            return false;
        }
        String name = v1StatefulSet.getMetadata().getName();
        int currentReplicas = v1StatefulSet.getSpec().getReplicas();
        String lastAppliedConfig = v1StatefulSet.getMetadata().getAnnotations()
                .get(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation());
        V1StatefulSet latAppliedStateFulSet = GsonProviderUtil.getPrettyGsonBuilder().fromJson(lastAppliedConfig, V1StatefulSet.class);
        ActivityContext activityContext = new ActivityContext(DeployerActivity.SCALING_SERVICE);
        WorkflowLogger.startActivity(activityContext);
        boolean status = false;
        try {
            if (!(currentReplicas == value && latAppliedStateFulSet.getSpec().getReplicas() == value)) {
                latAppliedStateFulSet.getSpec().setReplicas(value);
                patch(apiClient, name, namespace, latAppliedStateFulSet);
            }
            status = waitForDesiredState(apiClient, name, namespace, activityContext);
        } catch (HyscaleException e) {
            LOGGER.error("Error while applying PATCH scale to {} due to : {} code :{}", name, e.getMessage(), e.getCode(), e);
            WorkflowLogger.endActivity(Status.FAILED);
            HyscaleException ex = new HyscaleException(DeployerErrorCodes.ERROR_WHILE_SCALING, e.getMessage());
            throw ex;
        } finally {
            if (status) {
                if (value < currentReplicas) {
                    WorkflowLogger.persist(DeployerActivity.SCALE_DOWN_VOLUME,
                            v1StatefulSet.getSpec().getVolumeClaimTemplates().stream()
                                    .map(each -> each.getMetadata().getName()).collect(Collectors.joining(",")),
                            namespace);
                }
                WorkflowLogger.endActivity(activityContext, Status.DONE);
            } else {
                WorkflowLogger.endActivity(activityContext, Status.FAILED);
            }
        }
        return status;
    }

    private boolean waitForDesiredState(ApiClient apiClient, String name, String namespace,ActivityContext activityContext) throws HyscaleException {
        Long startTime = System.currentTimeMillis();
        boolean stable = false;
        int rotateThreshold = 5;
        int sleep = 3;
        try {
            int rotations = 0;
            while (System.currentTimeMillis() - startTime < DeployerConstants.MAX_WAIT_TIME_IN_MILLISECONDS) {
                V1StatefulSet updatedStatefulset = null;
                updatedStatefulset = get(apiClient, name, namespace);
                LOGGER.debug("Patched Statefulset status :{} ", updatedStatefulset.getStatus());
                if (status(updatedStatefulset) == ResourceStatus.STABLE) {
                    stable = true;
                    break;
                }
                WorkflowLogger.continueActivity(activityContext);
                if (rotateThreshold == rotations) {
                    sleep++;
                    rotations = 0;
                }
                Thread.sleep(sleep * 1000L);
                rotations++;
            }
        } catch (InterruptedException e) {
            LOGGER.error("Sleep Thread interrupted ", e);
            Thread.currentThread().interrupt();
        } catch (HyscaleException ex){
            LOGGER.error("Error while fetching statefulset {}", ex.getHyscaleError(), ex);
        }
        return stable;
    }

    @Override
    public boolean isWorkLoad() {
    	return true;
    }
}
