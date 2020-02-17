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

import io.hyscale.deployer.services.config.NonBeanDeployerConfig;
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
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.hyscale.deployer.services.util.ExceptionHelper;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import io.hyscale.deployer.services.util.K8sServiceUtil;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1LoadBalancerIngress;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceList;
import io.kubernetes.client.custom.V1Patch;

public class V1ServiceHandler implements ResourceLifeCycleHandler<V1Service> {

    private static final Logger LOGGER = LoggerFactory.getLogger(V1ServiceHandler.class);

    private static final long MAX_LB_READY_STATE_TIME = NonBeanDeployerConfig.getMaxLBReadyTimeout();
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
					AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(), gson.toJson(resource));
			v1Service = coreV1Api.createNamespacedService(namespace, resource, TRUE, null, null);
		} catch (ApiException e) {
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE,
					ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.CREATE));
			LOGGER.error("Error while creating Service {} in namespace {}, error {}", name, namespace, ex.toString());
			WorkflowLogger.endActivity(Status.FAILED);
			throw ex;
		}
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
			return service != null ? true : false;
		}
		WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_SERVICE);
		try {

			String resourceVersion = existingService.getMetadata().getResourceVersion();
			String clusterIP = existingService.getSpec().getClusterIP();
			resource.getMetadata().setResourceVersion(resourceVersion);
			resource.getSpec().setClusterIP(clusterIP);
			coreV1Api.replaceNamespacedService(name, namespace, resource, TRUE, null, null);
		} catch (ApiException e) {
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_UPDATE_RESOURCE,
					ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.UPDATE));
			LOGGER.error("Error while updating Service {} in namespace {}, error {}", name, namespace, ex.toString());
			WorkflowLogger.endActivity(Status.FAILED);
			throw ex;
		}

	WorkflowLogger.endActivity(Status.DONE);
	return true;
    }

    @Override
    public V1Service get(ApiClient apiClient, String name, String namespace) throws HyscaleException {
	CoreV1Api coreV1Api = new CoreV1Api(apiClient);
	V1Service v1Service = null;
	try {
	    v1Service = coreV1Api.readNamespacedService(name, namespace, TRUE, null, null);
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
	    V1ServiceList v1ServiceList = coreV1Api.listNamespacedService(namespace, null, null, fieldSelector,
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
				gson.toJson(target));
		V1Service sourceService = null;
		try {
			sourceService = get(apiClient, name, namespace);
		} catch (HyscaleException e) {
			LOGGER.debug("Error while getting Service {} in namespace {} for Patch, creating new", name, namespace);
			V1Service service = create(apiClient, target, namespace);
			return service != null ? true : false;
		}
		WorkflowLogger.startActivity(DeployerActivity.DEPLOYING_SERVICE);
		Object patchObject = null;
		String lastAppliedConfig = sourceService.getMetadata().getAnnotations()
				.get(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation());
		try {
			patchObject = K8sResourcePatchUtil.getJsonPatch(gson.fromJson(lastAppliedConfig, V1Service.class), target,
					V1Service.class);
			V1Patch v1Patch = new V1Patch(patchObject.toString());
			coreV1Api.patchNamespacedService(name, namespace, v1Patch, TRUE, null, null, null);
		} catch (HyscaleException ex) {
			LOGGER.error("Error while creating patch for Service {}, source {}, target {}, error", name, sourceService,
					target, ex.toString());
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
	CoreV1Api coreV1Api = new CoreV1Api(apiClient);

	V1DeleteOptions deleteOptions = getDeleteOptions();
	ActivityContext activityContext = new ActivityContext(DeployerActivity.DELETING_SERVICE);
	WorkflowLogger.startActivity(activityContext);
	try {
	    try {
			coreV1Api.deleteNamespacedService(name, namespace, TRUE, deleteOptions, null, null, null, null);
	    } catch (JsonSyntaxException e) {
		// K8s end exception ignore
	    }
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
	WorkflowLogger.endActivity(activityContext, Status.DONE);
	return true;
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
	    isSuccess = delete(apiClient, v1Service.getMetadata().getName(), namespace, wait) ? isSuccess : false;
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

	public ServiceAddress getServiceAddress(ApiClient apiClient, String name, String namespace, boolean wait)
			throws HyscaleException {

		if (!wait) {
			return getServiceAddress(apiClient, name, namespace);
		}
		long startTime = System.currentTimeMillis();
		V1Service v1Service = null;
		V1LoadBalancerIngress loadBalancerIngress = null;
		ActivityContext serviceIPContext = new ActivityContext(DeployerActivity.WAITING_FOR_SERVICE_IP);
		WorkflowLogger.startActivity(serviceIPContext);
		try {
			while (System.currentTimeMillis() - startTime < MAX_LB_READY_STATE_TIME) {
				WorkflowLogger.continueActivity(serviceIPContext);
				v1Service = get(apiClient, name, namespace);
				loadBalancerIngress = K8sServiceUtil.getLoadBalancer(v1Service);

				if (loadBalancerIngress != null) {
					break;
				}
				Thread.sleep(MAX_LB_WAIT_TIME);
			}
		} catch (InterruptedException e) {
			LOGGER.debug("Error while loadbalancer ready state condition");
		}
		if (loadBalancerIngress == null) {
			WorkflowLogger.endActivity(serviceIPContext, Status.FAILED);
			throw new HyscaleException(DeployerErrorCodes.FAILED_TO_GET_SERVICE_ADDRESS, getKind(), name, namespace);
		}
		WorkflowLogger.endActivity(serviceIPContext, Status.DONE);

		return K8sServiceUtil.getServiceAddress(v1Service);
	}

    private ServiceAddress getServiceAddress(ApiClient apiClient, String name, String namespace)
	    throws HyscaleException {

	V1Service service = get(apiClient, name, namespace);

	return K8sServiceUtil.getServiceAddress(service);

    }

}
