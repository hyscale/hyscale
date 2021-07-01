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

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.ActivityContext;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.GsonProviderUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.core.model.ResourceOperation;
import io.hyscale.deployer.services.constants.DeployerConstants;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.util.ExceptionHelper;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AutoscalingV1Api;
import io.kubernetes.client.openapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.kubernetes.client.custom.V1Patch;

import java.util.List;

/**
 * This class is responsible for the lifecycle operations of resource
 * HorizontalPodAutoScaler in kubernetes cluster. It creates resource of
 * apiVersion: autoscaling/v1
 * kind: HorizontalPodAutoScaler
 */

public class V1HorizontalPodAutoScalerHandler implements ResourceLifeCycleHandler<V1HorizontalPodAutoscaler> {

    private static final Logger logger = LoggerFactory.getLogger(V1HorizontalPodAutoScalerHandler.class);

    @Override
    public V1HorizontalPodAutoscaler create(ApiClient apiClient, V1HorizontalPodAutoscaler resource, String namespace) throws HyscaleException {
        if (resource == null) {
            logger.debug("Cannot create null HorizontalPodAutoScaler");
            return resource;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_HORIZONTAL_AUTO_SCALER);
        AutoscalingV1Api autoscalingV1Api = new AutoscalingV1Api(apiClient);
        V1HorizontalPodAutoscaler v1HorizontalPodAutoscaler = null;
        try {
            resource.getMetadata().putAnnotationsItem(
                    AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(), GsonProviderUtil.getPrettyGsonBuilder().toJson(resource));
            v1HorizontalPodAutoscaler = autoscalingV1Api.createNamespacedHorizontalPodAutoscaler(namespace, resource, DeployerConstants.TRUE, null, null);
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.CREATE));
            logger.error("Error while creating HorizontalPodAutoScaler {} in namespace {}, error {}", v1HorizontalPodAutoscaler, namespace,
                    ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(Status.DONE);
        return v1HorizontalPodAutoscaler;
    }

    @Override
    public boolean update(ApiClient apiClient, V1HorizontalPodAutoscaler resource, String namespace) throws HyscaleException {
        if (resource == null) {
            logger.debug("Cannot update null HorizontalPodAutoScaler");
            return false;
        }
        AutoscalingV1Api autoscalingV1Api = new AutoscalingV1Api(apiClient);
        String name = resource.getMetadata().getName();
        V1HorizontalPodAutoscaler existingHorizontalPodAutoscaler = null;
        try {
            existingHorizontalPodAutoscaler = get(apiClient, name, namespace);
        } catch (HyscaleException ex) {
            logger.debug("Error while getting HorizontalPodAutoScaler  {} in namespace {} for Update, creating new", name, namespace);
            V1HorizontalPodAutoscaler horizontalPodAutoscaler = create(apiClient, resource, namespace);
            return horizontalPodAutoscaler != null;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_HORIZONTAL_AUTO_SCALER);
        try {
            String resourceVersion = existingHorizontalPodAutoscaler.getMetadata().getResourceVersion();
            resource.getMetadata().setResourceVersion(resourceVersion);
            autoscalingV1Api.replaceNamespacedHorizontalPodAutoscaler(name, namespace, resource, DeployerConstants.TRUE, null, null);
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_UPDATE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.UPDATE));
            logger.error("Error while updating HorizontalPodAutoScaler {} in namespace {}, error {}", name, namespace,
                    ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

    @Override
    public V1HorizontalPodAutoscaler get(ApiClient apiClient, String name, String namespace) throws HyscaleException {
        AutoscalingV1Api autoscalingV1Api = new AutoscalingV1Api(apiClient);
        V1HorizontalPodAutoscaler horizontalPodAutoscaler = null;
        try {
            horizontalPodAutoscaler = autoscalingV1Api.readNamespacedHorizontalPodAutoscaler(name, namespace, DeployerConstants.TRUE, false, false);
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET);
            logger.error("Error while fetching HorizontalPodAutoScaler {} in namespace {}, error {} ", name, namespace,
                    ex.toString());
            throw ex;
        }
        return horizontalPodAutoscaler;
    }

    @Override
    public List<V1HorizontalPodAutoscaler> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace) throws HyscaleException {
        AutoscalingV1Api autoscalingV1Api = new AutoscalingV1Api(apiClient);
        List<V1HorizontalPodAutoscaler> v1HorizontalPodAutoscalers = null;
        try {
            String labelSelector = label ? selector : null;
            String fieldSelector = label ? null : selector;

            V1HorizontalPodAutoscalerList v1HorizontalPodAutoscalerList = autoscalingV1Api.listNamespacedHorizontalPodAutoscaler(namespace, DeployerConstants.TRUE,
                    null, null, fieldSelector, labelSelector, null, null, null, null, null);

            v1HorizontalPodAutoscalers = v1HorizontalPodAutoscalerList != null ? v1HorizontalPodAutoscalerList.getItems() : null;
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_BY_SELECTOR);
            logger.error("Error while listing HorizontalPodAutoScaler in namespace {}, with selectors {}, error {} ", namespace,
                    selector, ex.toString());
            throw ex;
        }
        return v1HorizontalPodAutoscalers;
    }

    @Override
    public boolean patch(ApiClient apiClient, String name, String namespace, V1HorizontalPodAutoscaler target) throws HyscaleException {
        if (target == null) {
            logger.debug("Cannot patch null HorizontalPodAutoScaler");
            return false;
        }
        AutoscalingV1Api autoscalingV1Api = new AutoscalingV1Api(apiClient);
        target.getMetadata().putAnnotationsItem(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                GsonProviderUtil.getPrettyGsonBuilder().toJson(target));
        V1HorizontalPodAutoscaler sourceHorizontalPodAutoScaler = null;
        try {
            sourceHorizontalPodAutoScaler = get(apiClient, name, namespace);
        } catch (HyscaleException e) {
            logger.debug("Error while getting HorizontalPodAutoScaler {} in namespace {} for Patch, creating new", name, namespace);
            V1HorizontalPodAutoscaler horizontalPodAutoscaler = create(apiClient, target, namespace);
            return horizontalPodAutoscaler != null;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_HORIZONTAL_AUTO_SCALER);
        Object patchObject = null;
        String lastAppliedConfig = sourceHorizontalPodAutoScaler.getMetadata().getAnnotations()
                .get(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation());
        try {
            patchObject = K8sResourcePatchUtil.getJsonPatch(GsonProviderUtil.getPrettyGsonBuilder().fromJson(lastAppliedConfig, V1HorizontalPodAutoscaler.class),
                    target, V1HorizontalPodAutoscaler.class);
            V1Patch v1Patch = new V1Patch(patchObject.toString());
            autoscalingV1Api.patchNamespacedHorizontalPodAutoscaler(name, namespace, v1Patch, DeployerConstants.TRUE, null, null, null);
        } catch (HyscaleException e) {
            logger.error("Error while creating patch for HorizontalPodAutoScaler {}, source {}, target {}", name, sourceHorizontalPodAutoScaler,
                    target);
            WorkflowLogger.endActivity(Status.FAILED);
            throw e;
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_PATCH_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.PATCH));
            logger.error("Error while patching HorizontalPodAutoScaler {} in namespace {} , error {}", name, namespace,
                    ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

    @Override
    public boolean delete(ApiClient apiClient, String name, String namespace, boolean wait) throws HyscaleException {
        ActivityContext activityContext = new ActivityContext(DeployerActivity.DELETING_HORIZONTAL_POD_AUTOSCALER);
        WorkflowLogger.startActivity(activityContext);
        try {
            delete(apiClient, name, namespace);
            List<String> pendingHPAs = Lists.newArrayList();
            pendingHPAs.add(name);
            if (wait) {
                waitForResourceDeletion(apiClient, pendingHPAs, namespace, activityContext);
            }
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                WorkflowLogger.endActivity(activityContext, Status.NOT_FOUND);
                return false;
            }
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.DELETE));
            logger.error("Error while deleting HorizontalPodAutoScaler {} in namespace {}, error {} ", name, namespace,
                    ex.toString());
            WorkflowLogger.endActivity(activityContext, Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(activityContext, Status.DONE);
        return true;
    }

    private void delete(ApiClient apiClient, String name, String namespace) throws ApiException {
        AutoscalingV1Api autoscalingV1Api = new AutoscalingV1Api(apiClient);
        V1DeleteOptions deleteOptions = getDeleteOptions();
        deleteOptions.setApiVersion("autoscaling/v1");
        try {
            autoscalingV1Api.deleteNamespacedHorizontalPodAutoscaler(name, namespace, DeployerConstants.TRUE, null,
                    null, null, null, deleteOptions);
        } catch (JsonSyntaxException e) {
            logger.debug("Ignoring delete HorizontalPodAutoScaler exception");
        }
    }

    @Override
    public boolean deleteBySelector(ApiClient apiClient, String selector, boolean label, String namespace, boolean wait) throws HyscaleException {
        V1DeleteOptions deleteOptions = getDeleteOptions();
        deleteOptions.setApiVersion("autoscaling/v1");
        try {
            List<V1HorizontalPodAutoscaler> horizontalPodAutoscalerList = getBySelector(apiClient, selector, label, namespace);
            if (horizontalPodAutoscalerList == null || horizontalPodAutoscalerList.isEmpty()) {
                return false;
            }
            for (V1HorizontalPodAutoscaler horizontalPodAutoscaler : horizontalPodAutoscalerList) {
                delete(apiClient, horizontalPodAutoscaler.getMetadata().getName(), namespace, wait);
            }
        } catch (HyscaleException e) {
            if (DeployerErrorCodes.RESOURCE_NOT_FOUND.equals(e.getHyscaleError())) {
                logger.error("Error while deleting HorizontalPodAutoScaler for selector {} in namespace {}, error {}", selector,
                        namespace, e.toString());
                return false;
            }
            throw e;
        }
        return true;
    }

    @Override
    public String getKind() {
        return ResourceKind.HORIZONTAL_POD_AUTOSCALER.getKind();
    }

    @Override
    public int getWeight() {
        return ResourceKind.HORIZONTAL_POD_AUTOSCALER.getWeight();
    }

    @Override
    public boolean cleanUp() {
        return true;
    }
}
