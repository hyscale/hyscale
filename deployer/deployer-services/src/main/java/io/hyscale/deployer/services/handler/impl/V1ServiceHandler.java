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

import io.hyscale.deployer.services.config.DeployerEnvConfig;
import io.hyscale.deployer.services.constants.DeployerConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.hyscale.deployer.services.util.ExceptionHelper;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import io.hyscale.deployer.services.util.K8sServiceUtil;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1LoadBalancerIngress;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.custom.V1Patch;

public class V1ServiceHandler implements ResourceLifeCycleHandler<V1Service> {

    private static final Logger LOGGER = LoggerFactory.getLogger(V1ServiceHandler.class);

    private static final long LB_READY_STATE_TIME = DeployerEnvConfig.getLBReadyTimeout();
    private static final long MAX_LB_WAIT_TIME = 2000;

    @Override
    public V1Service create(ApiClient apiClient, V1Service resource, String namespace) throws HyscaleException {
        if (resource == null) {
            LOGGER.debug("Cannot create null Service");
            return resource;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_SERVICE);
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        String name = resource.getMetadata().getName();
        V1Service v1Service = null;
        try {
            resource.getMetadata().putAnnotationsItem(
                    AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(), GsonProviderUtil.getPrettyGsonBuilder().toJson(resource));
            v1Service = coreV1Api.createNamespacedService(namespace, resource, DeployerConstants.TRUE, null, null);
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.CREATE));
            LOGGER.error("Error while creating Service {} in namespace {}, error {}", name, namespace, ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        LOGGER.info("Created Service {} in namespace {}", name, namespace);
        WorkflowLogger.endActivity(Status.DONE);
        return v1Service;
    }

    @Override
    public boolean update(ApiClient apiClient, V1Service resource, String namespace) throws HyscaleException {
        if (resource == null) {
            LOGGER.debug("Cannot update null Service");
            return false;
        }
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        String name = resource.getMetadata().getName();
        V1Service existingService = null;
        try {
            existingService = get(apiClient, name, namespace);
        } catch (HyscaleException ex) {
            LOGGER.debug("Error while getting Service {} in namespace {} for Update, creating new", name, namespace);
            V1Service service = create(apiClient, resource, namespace);
            return service != null;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_SERVICE);
        try {

            String resourceVersion = existingService.getMetadata().getResourceVersion();
            String clusterIP = existingService.getSpec().getClusterIP();
            resource.getMetadata().setResourceVersion(resourceVersion);
            resource.getSpec().setClusterIP(clusterIP);
            coreV1Api.replaceNamespacedService(name, namespace, resource, DeployerConstants.TRUE, null, null);
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_UPDATE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.UPDATE));
            LOGGER.error("Error while updating Service {} in namespace {}, error {}", name, namespace, ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        LOGGER.info("Updated Service {} in namespace {}", name, namespace);
        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

    @Override
    public V1Service get(ApiClient apiClient, String name, String namespace) throws HyscaleException {
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        V1Service v1Service = null;
        try {
            v1Service = coreV1Api.readNamespacedService(name, namespace, DeployerConstants.TRUE, null, null);
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET);
            LOGGER.error("Error while fetching Service {} in namespace {}, error {} ", name, namespace, ex.toString());
            throw ex;
        }
        return v1Service;
    }

    @Override
    public List<V1Service> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace)
            throws HyscaleException {
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        List<V1Service> v1Services = null;
        try {
            String labelSelector = label ? selector : null;
            String fieldSelector = label ? null : selector;
            V1ServiceList v1ServiceList = coreV1Api.listNamespacedService(namespace, DeployerConstants.TRUE, null, null, fieldSelector,
                    labelSelector, null, null, null, null);
            v1Services = v1ServiceList != null ? v1ServiceList.getItems() : null;
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_BY_SELECTOR);
            LOGGER.error("Error while listing Services in namespace {}, with selectors {}, error {} ", namespace,
                    selector, ex.toString());
            throw ex;
        }
        return v1Services;
    }

    @Override
    public boolean patch(ApiClient apiClient, String name, String namespace, V1Service target) throws HyscaleException {
        if (target == null) {
            LOGGER.debug("Cannot patch null Service");
            return false;
        }
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        target.getMetadata().putAnnotationsItem(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                GsonProviderUtil.getPrettyGsonBuilder().toJson(target));
        V1Service sourceService = null;
        try {
            sourceService = get(apiClient, name, namespace);
        } catch (HyscaleException e) {
            LOGGER.debug("Error while getting Service {} in namespace {} for Patch, creating new", name, namespace);
            V1Service service = create(apiClient, target, namespace);
            return service != null;
        }
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_SERVICE);
        Object patchObject = null;
        String lastAppliedConfig = sourceService.getMetadata().getAnnotations()
                .get(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation());
        try {
            patchObject = K8sResourcePatchUtil.getJsonPatch(GsonProviderUtil.getPrettyGsonBuilder().fromJson(lastAppliedConfig, V1Service.class), target,
                    V1Service.class);
            V1Patch v1Patch = new V1Patch(patchObject.toString());
            coreV1Api.patchNamespacedService(name, namespace, v1Patch, DeployerConstants.TRUE, null, null, null);
        } catch (HyscaleException ex) {
            LOGGER.error("Error while creating patch for Service {}, source {}, target {}", name, sourceService, target,
                    ex);
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        } catch (ApiException e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_PATCH_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.PATCH));
            LOGGER.error("Error while patching Service {} in namespace {} , error {}", name, namespace, ex.toString());
            WorkflowLogger.endActivity(Status.FAILED);
            throw ex;
        }
        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

    @Override
    public boolean delete(ApiClient apiClient, String name, String namespace, boolean wait) throws HyscaleException {
        ActivityContext activityContext = new ActivityContext(DeployerActivity.DELETING_SERVICE);
        WorkflowLogger.startActivity(activityContext);
        try {
            delete(apiClient, name, namespace);
            List<String> serviceList = Lists.newArrayList();
            serviceList.add(name);
            if (wait) {
                waitForResourceDeletion(apiClient, serviceList, namespace, activityContext);
            }
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                WorkflowLogger.endActivity(activityContext, Status.NOT_FOUND);
                return false;
            }
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE,
                    ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.DELETE));
            LOGGER.error("Error while deleting Service {} in namespace {} error {}", name, namespace, ex.toString());
            WorkflowLogger.endActivity(activityContext, Status.FAILED);
            throw ex;
        }
        LOGGER.info("Deleted Service {} in namespace {}",name, namespace);
        WorkflowLogger.endActivity(activityContext, Status.DONE);
        return true;
    }

    private void delete(ApiClient apiClient, String name, String namespace) throws ApiException {
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        V1DeleteOptions deleteOptions = getDeleteOptions();
        try {
            coreV1Api.deleteNamespacedService(name, namespace, DeployerConstants.TRUE, null, null, null, null,
                    deleteOptions);
        } catch (JsonSyntaxException e) {
            // K8s end exception ignore
        }
    }

    @Override
    public boolean deleteBySelector(ApiClient apiClient, String selector, boolean label, String namespace, boolean wait)
            throws HyscaleException {
        boolean isSuccess = true;
        List<V1Service> v1Services = getBySelector(apiClient, selector, label, namespace);
        if (v1Services == null || v1Services.isEmpty()) {
            return false;
        }
        for (V1Service v1Service : v1Services) {
            boolean isDeleted = delete(apiClient, v1Service.getMetadata().getName(), namespace, wait);
            isSuccess = isSuccess && isDeleted;
        }
        return isSuccess;
    }

    @Override
    public String getKind() {
        return ResourceKind.SERVICE.getKind();
    }

    @Override
    public boolean cleanUp() {
        return true;
    }

    @Override
    public int getWeight() {
        return ResourceKind.SERVICE.getWeight();
    }

    public ServiceAddress getServiceAddress(ApiClient apiClient, String selector, String namespace, boolean wait)
            throws HyscaleException {

        if (!wait) {
            return getServiceAddress(apiClient, selector, namespace);
        }
        long startTime = System.currentTimeMillis();
        V1Service v1Service = null;
        V1LoadBalancerIngress loadBalancerIngress = null;
        ActivityContext serviceIPContext = new ActivityContext(DeployerActivity.WAITING_FOR_SERVICE_IP);
        WorkflowLogger.startActivity(serviceIPContext);
        try {
            while (System.currentTimeMillis() - startTime < LB_READY_STATE_TIME) {
                WorkflowLogger.continueActivity(serviceIPContext);
                List<V1Service> v1ServiceList = getBySelector(apiClient, selector, true, namespace);
                if (v1ServiceList != null && !v1ServiceList.isEmpty()){
                    v1Service = v1ServiceList.get(0);
                    loadBalancerIngress = K8sServiceUtil.getLoadBalancer(v1Service);
                }
                if (loadBalancerIngress != null || v1ServiceList == null || v1ServiceList.isEmpty()) {
                    break;
                }
                Thread.sleep(MAX_LB_WAIT_TIME);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Error while loadbalancer ready state condition", e);
        } catch (HyscaleException e) {
            LOGGER.error("Error while loadbalancer ready state condition", e);
            WorkflowLogger.endActivity(serviceIPContext, Status.FAILED);
            throw new HyscaleException(DeployerErrorCodes.FAILED_TO_GET_SERVICE_ADDRESS);
        }
        if (loadBalancerIngress == null) {
            WorkflowLogger.endActivity(serviceIPContext, Status.FAILED);
            throw new HyscaleException(DeployerErrorCodes.FAILED_TO_GET_SERVICE_ADDRESS);
        }
        WorkflowLogger.endActivity(serviceIPContext, Status.DONE);

        return K8sServiceUtil.getServiceAddress(v1Service);
    }

    private ServiceAddress getServiceAddress(ApiClient apiClient, String selector, String namespace)
            throws HyscaleException {
        
        List<V1Service> v1ServiceList = getBySelector(apiClient, selector, true, namespace);
        
        V1Service service = v1ServiceList != null && !v1ServiceList.isEmpty() ? v1ServiceList.get(0) : null;

        if (service == null) {
            LOGGER.debug("No service found for selector {} in namespace {}, returning null", selector, namespace);
            return null;
        }
        return K8sServiceUtil.getServiceAddress(service);

    }

}
