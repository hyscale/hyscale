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
/**
 *
 */
package io.hyscale.deployer.services.handler.impl;

import java.util.List;

import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.util.ExceptionHelper;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import io.hyscale.deployer.services.util.KubernetesApiProvider;
import io.hyscale.deployer.services.util.KubernetesResourceUtil;

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
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Secret;
import io.kubernetes.client.models.V1SecretList;

// TODO Integrate logging with AOP
public class V1SecretHandler implements ResourceLifeCycleHandler<V1Secret> {

    private static final Logger LOGGER = LoggerFactory.getLogger(V1SecretHandler.class);

    @Override
    public V1Secret create(ApiClient apiClient, V1Secret resource, String namespace) throws HyscaleException {
        if (resource == null) {
            LOGGER.debug("Cannot create null Secret");
            return resource;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_SECRETS);
        V1ObjectMeta metadata = resource.getMetadata();
        try {
            KubernetesResourceUtil.isResourceValid(apiClient, getKind(), metadata);
        } catch (HyscaleException e) {
            WorkflowLogger.endActivity(Status.FAILED);
            throw e;
        }
        CoreV1Api coreV1Api = KubernetesApiProvider.getCoreV1Api(apiClient);
        String name = metadata.getName();
        V1Secret v1Secret = null;
        try {
            metadata.putAnnotationsItem(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                    gson.toJson(resource));
            v1Secret = coreV1Api.createNamespacedSecret(namespace, resource, null, TRUE, null);
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE,
                    ExceptionHelper.getExceptionArgs(getKind(), e, ResourceOperation.CREATE));
            LOGGER.error("Error while creating Secret {} in namespace {}, error {}", name, namespace, ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(Status.DONE);
        return v1Secret;
    }

    @Override
    public boolean update(ApiClient apiClient, V1Secret resource, String namespace) throws HyscaleException {
        if (resource == null) {
            LOGGER.debug("Cannot update null Secret");
            return false;
        }
        V1ObjectMeta metadata = resource.getMetadata();
        try {
            KubernetesResourceUtil.isResourceValid(apiClient, getKind(), metadata);
        } catch (HyscaleException e) {
            WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_SECRETS);
            WorkflowLogger.endActivity(Status.FAILED);
            throw e;
        }
        CoreV1Api coreV1Api = KubernetesApiProvider.getCoreV1Api(apiClient);
        String name = metadata.getName();
        V1Secret existingSecret = null;
        try {
            existingSecret = get(apiClient, name, namespace);
        } catch (HyscaleException ex) {
            LOGGER.debug("Error while getting Secret {} in namespace {} for Update, creating new", name, namespace);
            V1Secret secret = create(apiClient, resource, namespace);
            return secret != null ? true : false;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_SECRETS);
        try {

            String resourceVersion = existingSecret.getMetadata().getResourceVersion();
            metadata.setResourceVersion(resourceVersion);
            coreV1Api.replaceNamespacedSecret(name, namespace, resource, TRUE, null);
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_UPDATE_RESOURCE,
                    ExceptionHelper.getExceptionArgs(getKind(), e, ResourceOperation.UPDATE));
            LOGGER.error("Error while updating Secret {} in namespace {}, error {}", name, namespace, ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

    @Override
    public V1Secret get(ApiClient apiClient, String name, String namespace) throws HyscaleException {
        KubernetesResourceUtil.isResourceValid(apiClient, getKind(), name);
        V1Secret v1Secret = null;
        CoreV1Api apiInstance = KubernetesApiProvider.getCoreV1Api(apiClient);
        try {
            v1Secret = apiInstance.readNamespacedSecret(name, namespace, TRUE, null, null);
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET);
            LOGGER.error("Error while fetching Secret {} in namespace {}, error {}", name, namespace, ex.toString());
            throw ex;
        }
        return v1Secret;
    }

    @Override
    public List<V1Secret> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace)
            throws HyscaleException {
        if (apiClient == null) {
            throw new HyscaleException(DeployerErrorCodes.API_CLIENT_REQUIRED);
        }
        CoreV1Api coreV1Api = KubernetesApiProvider.getCoreV1Api(apiClient);
        List<V1Secret> v1Secrets = null;
        try {
            String labelSelector = label ? selector : null;
            String fieldSelector = label ? null : selector;
            V1SecretList v1SecretList = coreV1Api.listNamespacedSecret(namespace, null, TRUE, null, fieldSelector,
                    labelSelector, null, null, null, null);
            v1Secrets = v1SecretList != null ? v1SecretList.getItems() : null;
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_BY_SELECTOR);
            LOGGER.error("Error while listing Secrets in namespace {}, with selectors {}, error {} ", namespace,
                    selector, ex.toString());
            throw ex;
        }
        return v1Secrets;
    }

    @Override
    public boolean patch(ApiClient apiClient, String name, String namespace, V1Secret target) throws HyscaleException {
        if (target == null) {
            LOGGER.debug("Cannot patch null Secret");
            return false;
        }
        V1ObjectMeta metadata = target.getMetadata();
        try {
            KubernetesResourceUtil.isResourceValid(apiClient, getKind(), metadata, name);
        } catch (HyscaleException e) {
            WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_SECRETS);
            WorkflowLogger.endActivity(Status.FAILED);
            throw e;
        }
        CoreV1Api coreV1Api = KubernetesApiProvider.getCoreV1Api(apiClient);
        metadata.putAnnotationsItem(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                gson.toJson(target));
        V1Secret sourceSecret = null;
        try {
            sourceSecret = get(apiClient, name, namespace);
        } catch (HyscaleException e) {
            LOGGER.debug("Error while getting Secret {} in namespace {} for Patch, creating new", name, namespace);
            V1Secret secret = create(apiClient, target, namespace);
            return secret != null ? true : false;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_SECRETS);
        Object patchObject = null;
        String lastAppliedConfig = sourceSecret.getMetadata().getAnnotations()
                .get(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation());
        try {
            patchObject = K8sResourcePatchUtil.getJsonPatch(gson.fromJson(lastAppliedConfig, V1Secret.class), target,
                    V1Secret.class);
            coreV1Api.patchNamespacedSecret(name, namespace, patchObject, TRUE, null);
        } catch (HyscaleException ex) {
            LOGGER.error("Error while creating patch for Secret {}, source {}, target {}, error {}", name, sourceSecret,
                    target, ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_PATCH_RESOURCE,
                    ExceptionHelper.getExceptionArgs(getKind(), e, ResourceOperation.PATCH));
            LOGGER.error("Error while patching Secret {} in namespace {} , error {}", name, namespace, ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

    @Override
    public boolean delete(ApiClient apiClient, String name, String namespace, boolean wait) throws HyscaleException {
        ActivityContext activityContext = new ActivityContext(DeployerActivity.DELETING_SECRETS);
        WorkflowLogger.startActivity(activityContext);

        try {
            KubernetesResourceUtil.isResourceValid(apiClient, getKind(), name);
        } catch (HyscaleException e) {
            WorkflowLogger.endActivity(activityContext, Status.FAILED);
            throw e;
        }
        CoreV1Api coreV1Api = KubernetesApiProvider.getCoreV1Api(apiClient);
        V1DeleteOptions deleteOptions = getDeleteOptions();
        try {
            try {
                coreV1Api.deleteNamespacedSecret(name, namespace, deleteOptions, TRUE, null, null, null, null);
            } catch (JsonSyntaxException e) {
                // K8s end exception ignore
            }
            List<String> secretList = Lists.newArrayList();
            secretList.add(name);
            if (wait) {
                waitForResourceDeletion(apiClient, secretList, namespace, activityContext);
            }
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                WorkflowLogger.endActivity(activityContext, Status.NOT_FOUND);
                return false;
            }
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE,
                    ExceptionHelper.getExceptionArgs(getKind(), e, ResourceOperation.DELETE));
            LOGGER.error("Error while deleting Secret {} in namespace {}, error {}", name, namespace, ex.toString());
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
            List<V1Secret> V1SecretList = getBySelector(apiClient, selector, label, namespace);
            if (V1SecretList == null || V1SecretList.isEmpty()) {
                return false;
            }
            for (V1Secret V1Secret : V1SecretList) {
                delete(apiClient, V1Secret.getMetadata().getName(), namespace, wait);
            }
        } catch (HyscaleException e) {
            if (DeployerErrorCodes.RESOURCE_NOT_FOUND.equals(e.getHyscaleErrorCode())) {
                LOGGER.error("Error while deleting Secrets for selector {} in namespace {}, error {}", selector,
                        namespace, e.toString());
                return false;
            }
            throw e;
        }
        return true;
    }

    @Override
    public String getKind() {
        return ResourceKind.SECRET.getKind();
    }

    @Override
    public boolean cleanUp() {
        return true;
    }

    @Override
    public int getWeight() {
        return ResourceKind.SECRET.getWeight();
    }

}
