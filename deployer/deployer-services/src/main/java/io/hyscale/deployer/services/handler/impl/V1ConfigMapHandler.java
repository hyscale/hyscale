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

import java.util.List;

import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.model.ResourceStatus;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.util.ExceptionHelper;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.ActivityContext;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.Status;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.core.model.ResourceOperation;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1ConfigMap;
import io.kubernetes.client.models.V1ConfigMapList;
import io.kubernetes.client.models.V1DeleteOptions;

public class V1ConfigMapHandler implements ResourceLifeCycleHandler<V1ConfigMap> {

    private static final Logger LOGGER = LoggerFactory.getLogger(V1ConfigMapHandler.class);

    @Override
    public V1ConfigMap create(ApiClient apiClient, V1ConfigMap resource, String namespace) throws HyscaleException {
        if (resource == null) {
            LOGGER.debug("Cannot create null ConfigMap");
            return resource;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_CONFIGMAP);
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        String name = resource.getMetadata().getName();
        V1ConfigMap configMap = null;
        try {
            resource.getMetadata().putAnnotationsItem(
                    AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(), gson.toJson(resource));
            configMap = coreV1Api.createNamespacedConfigMap(namespace, resource, null, TRUE, null);
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.CREATE));
            LOGGER.error("Error while creating ConfigMap {} in namespace {}, error {}", name, namespace, ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(Status.DONE);
        return configMap;
    }

    /**
     * Fetch from cluster, populate required field in resource call update
     */
    @Override
    public boolean update(ApiClient apiClient, V1ConfigMap resource, String namespace) throws HyscaleException {
        if(resource==null){
            LOGGER.debug("Cannot update null ConfigMap");
            return false;
        }
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        String name = resource.getMetadata().getName();
        V1ConfigMap existingConfigMap = null;
        try {
            existingConfigMap = get(apiClient, name, namespace);
        } catch (HyscaleException ex) {
            LOGGER.debug("Error while getting ConfigMap {} in namespace {} for Update, creating new", name, namespace);
            V1ConfigMap configMap = create(apiClient, resource, namespace);
            return configMap != null ? true : false;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_CONFIGMAP);
        try {
            String resourceVersion = existingConfigMap.getMetadata().getResourceVersion();
            resource.getMetadata().setResourceVersion(resourceVersion);
            coreV1Api.replaceNamespacedConfigMap(name, namespace, resource, TRUE, null);
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_UPDATE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.UPDATE));
            LOGGER.error("Error while updating ConfigMap {} in namespace {}, error {}", name, namespace, ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }

        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

    @Override
    public V1ConfigMap get(ApiClient apiClient, String name, String namespace) throws HyscaleException {
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        V1ConfigMap configMap = null;
        try {
            configMap = coreV1Api.readNamespacedConfigMap(name, namespace, TRUE, null, null);
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET);
            LOGGER.error("Error while fetching ConfigMap {} in namespace {}, error {}", name, namespace, ex.toString());
            throw ex;
        }
        return configMap;
    }

    @Override
    public List<V1ConfigMap> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace)
            throws HyscaleException {
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        List<V1ConfigMap> configMaps = null;
        try {
            String labelSelector = label ? selector : null;
            String fieldSelector = label ? null : selector;

            V1ConfigMapList configMapList = coreV1Api.listNamespacedConfigMap(namespace, null, TRUE, null,
                    fieldSelector, labelSelector, null, null, null, null);
            configMaps = configMapList != null ? configMapList.getItems() : null;
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_BY_SELECTOR);
            LOGGER.error("Error while listing ConfigMaps in namespace {}, with selectors {}, error {} ", namespace,
                    selector, ex.toString());
            throw ex;
        }
        return configMaps;
    }

    /**
     * Fetch existing resource, create diff, call patch api with diff
     */
    @Override
    public boolean patch(ApiClient apiClient, String name, String namespace, V1ConfigMap target)
            throws HyscaleException {
        if (target == null) {
            LOGGER.debug("Cannot patch null configmap");
            return false;
        }
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        target.getMetadata().putAnnotationsItem(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                gson.toJson(target));
        V1ConfigMap sourceConfigMap = null;
        try {
            sourceConfigMap = get(apiClient, name, namespace);
        } catch (HyscaleException e) {
            LOGGER.debug("Error while getting ConfigMap {} in namespace {} for Patch, creating new", name, namespace);
            V1ConfigMap configMap = create(apiClient, target, namespace);
            return configMap != null ? true : false;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_CONFIGMAP);
        String lastAppliedConfig = sourceConfigMap.getMetadata().getAnnotations()
                .get(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation());
        Object patchObject = null;
        try {
            patchObject = K8sResourcePatchUtil.getJsonPatch(gson.fromJson(lastAppliedConfig, V1ConfigMap.class), target,
                    V1ConfigMap.class);
            coreV1Api.patchNamespacedConfigMap(name, namespace, patchObject, TRUE, null);
        } catch (HyscaleException e) {
            LOGGER.error("Error while creating patch for ConfigMap {}, source {}, target {}", name, sourceConfigMap,
                    target);
            WorkflowLogger.endActivity(Status.FAILED);
            throw e;
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_PATCH_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.PATCH));
            LOGGER.error("Error while patching ConfigMap {} in namespace {} , error {}", name, namespace,
                    ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

    @Override
    public boolean delete(ApiClient apiClient, String name, String namespace, boolean wait) throws HyscaleException {
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);

        V1DeleteOptions deleteOptions = getDeleteOptions();
        ActivityContext activityContext = new ActivityContext(DeployerActivity.DELETING_CONFIG_MAP);
        WorkflowLogger.startActivity(activityContext);
        try {
            try {
                coreV1Api.deleteNamespacedConfigMap(name, namespace, TRUE,deleteOptions, null, null, null, null);
            } catch (JsonSyntaxException e) {
                // K8s end exception ignore
            }
            List<String> configmapList = Lists.newArrayList();
            configmapList.add(name);
            if (wait) {
                waitForResourceDeletion(apiClient, configmapList, namespace, activityContext);
            }
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                WorkflowLogger.endActivity(activityContext, Status.NOT_FOUND);
                return false;
            }
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.DELETE));
            LOGGER.error("Error while deleting ConfigMap {} in namespace {}, error {}", name, namespace, ex.toString());
            WorkflowLogger.endActivity(activityContext, Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(activityContext, Status.DONE);
        return true;
    }

    @Override
    public boolean deleteBySelector(ApiClient apiClient, String selector, boolean label, String namespace, boolean wait)
            throws HyscaleException {
        try {
            List<V1ConfigMap> configMapsList = getBySelector(apiClient, selector, label, namespace);
            if (configMapsList == null || configMapsList.isEmpty()) {
                return false;
            }
            for (V1ConfigMap configMap : configMapsList) {
                delete(apiClient, configMap.getMetadata().getName(), namespace, wait);
            }
        } catch (HyscaleException e) {
            if (DeployerErrorCodes.RESOURCE_NOT_FOUND.equals(e.getHyscaleErrorCode())) {
                LOGGER.error("Error while deleting ConfigMap for selector {} in namespace {}, error {}", selector,
                        namespace, e.toString());
                return false;
            }
            throw e;
        }
        return true;
    }

    @Override
    public String getKind() {
        return ResourceKind.CONFIG_MAP.getKind();
    }

    @Override
    public boolean cleanUp() {
        return true;
    }

    @Override
    public int getWeight() {
        return ResourceKind.CONFIG_MAP.getWeight();
    }

	public ResourceStatus status(V1ConfigMap v1ConfigMap){
		return ResourceStatus.STABLE;
	}
}
