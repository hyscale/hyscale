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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.ActivityContext;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.ResourceLabelKey;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.core.model.ResourceOperation;
import io.hyscale.deployer.services.config.DeployerEnvConfig;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.factory.PodParentFactory;
import io.hyscale.deployer.services.handler.PodParentHandler;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.model.PodParent;
import io.hyscale.deployer.services.predicates.PodPredicates;
import io.hyscale.deployer.services.util.ExceptionHelper;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import io.kubernetes.client.PodLogs;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.util.Watch;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Response;

public class V1PodHandler implements ResourceLifeCycleHandler<V1Pod> {

	private static final Logger LOGGER = LoggerFactory.getLogger(V1PodHandler.class);
	private static final long MAX_TIME_TO_CONTAINER_READY = 120 * 1000;
	private static final long POD_RESTART_COUNT = DeployerEnvConfig.getPodRestartCount();
	private static final Integer POD_ACTIVITY_WAITING_TIME =100;

	
	@Override
	public V1Pod create(ApiClient apiClient, V1Pod resource, String namespace) throws HyscaleException {
		if (resource == null) {
			LOGGER.debug("Cannot create null Pod");
			return resource;
		}
		CoreV1Api coreV1Api = new CoreV1Api(apiClient);
		String name = resource.getMetadata().getName();
		V1Pod v1Pod = null;
		try {
			resource.getMetadata().putAnnotationsItem(
					AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(), gson.toJson(resource));
			v1Pod = coreV1Api.createNamespacedPod(namespace, resource, TRUE, null, null);
		} catch (ApiException e) {
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE,
					ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.CREATE));
			LOGGER.error("Error while creating Pod {} in namespace {}, error {}", name, namespace, ex.toString());
			throw ex;
		}
		return v1Pod;

	}

	@Override
	public boolean update(ApiClient apiClient, V1Pod resource, String namespace) throws HyscaleException {
		if(resource==null){
			LOGGER.debug("Cannot update null Pod");
			return false;
		}
		CoreV1Api coreV1Api = new CoreV1Api(apiClient);
		String name = resource.getMetadata().getName();
		V1Pod existingPod = null;

		try {
			existingPod = get(apiClient, name, namespace);
		} catch (HyscaleException ex) {
			LOGGER.debug("Error while getting Pod {} in namespace {} for Update, creating new", name, namespace);
			V1Pod pod = create(apiClient, resource, namespace);
			return pod != null ? true : false;
		}
		try {
			String resourceVersion = existingPod.getMetadata().getResourceVersion();
			resource.getMetadata().setResourceVersion(resourceVersion);
			coreV1Api.replaceNamespacedPod(name, namespace, resource, TRUE, null, null);
		} catch (ApiException e) {
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_UPDATE_RESOURCE,
					ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.UPDATE));
			LOGGER.error("Error while updating Pod {} in namespace {}, error {}", name, namespace, ex.toString());
			throw ex;
		}

		return true;
	}

	@Override
	public V1Pod get(ApiClient apiClient, String name, String namespace) throws HyscaleException {
		CoreV1Api coreV1Api = new CoreV1Api(apiClient);
		V1Pod v1Pod = null;
		try {
			v1Pod = coreV1Api.readNamespacedPod(name, namespace, TRUE, null, null);
		} catch (ApiException e) {
			HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET);
			LOGGER.error("Error while fetching Pod {} in namespace {}, error {}", name, namespace, ex.toString());
			throw ex;
		}
		return v1Pod;
	}

	@Override
	public List<V1Pod> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace)
			throws HyscaleException {
		CoreV1Api coreV1Api = new CoreV1Api(apiClient);
		String labelSelector = label ? selector : null;
		String fieldSelector = label ? null : selector;
		List<V1Pod> v1Pods = null;
		try {
			V1PodList v1PodList = coreV1Api.listNamespacedPod(namespace, TRUE, null, null, fieldSelector, labelSelector,
					null, null, null, null);
			v1Pods = v1PodList != null ? v1PodList.getItems() : null;
		} catch (ApiException e) {
			HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_BY_SELECTOR);
			LOGGER.error("Error while listing Pods in namespace {}, with selectors {},  error {}", namespace, selector,
					ex.toString());
			throw ex;
		}
		return v1Pods;
	}

	@Override
	public boolean patch(ApiClient apiClient, String name, String namespace, V1Pod target) throws HyscaleException {
		if (target == null) {
			LOGGER.debug("Cannot patch null Pod");
			return false;
		}
		CoreV1Api coreV1Api = new CoreV1Api(apiClient);
		target.getMetadata().putAnnotationsItem(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
				gson.toJson(target));
		V1Pod sourcePod = null;
		try {
			sourcePod = get(apiClient, name, namespace);
		} catch (HyscaleException e) {
			LOGGER.debug("Error while getting Pod {} in namespace {} for Patch, creating new", name, namespace);
			V1Pod pod = create(apiClient, target, namespace);
			return pod != null ? true : false;
		}
		Object patchObject = null;
		String lastAppliedConfig = sourcePod.getMetadata().getAnnotations()
				.get(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation());
		try {
			patchObject = K8sResourcePatchUtil.getJsonPatch(gson.fromJson(lastAppliedConfig, V1Pod.class), target,
					V1Pod.class);
			V1Patch v1Patch =  new V1Patch(patchObject.toString());
			coreV1Api.patchNamespacedPod(name, namespace, v1Patch, TRUE, null, null, null);
		} catch (HyscaleException e) {
			LOGGER.error("Error while creating patch for Pod {}, source {}, target {}", name, sourcePod, target);
			throw e;
		} catch (ApiException e) {
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_PATCH_RESOURCE,
					ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.PATCH));
			LOGGER.error("Error while patching Pod {} in namespace {} , error {}", name, namespace, ex.toString());
			throw ex;
		}
		return true;
	}

	@Override
	public boolean delete(ApiClient apiClient, String name, String namespace, boolean wait) throws HyscaleException {
		CoreV1Api coreV1Api = new CoreV1Api(apiClient);

		V1DeleteOptions deleteOptions = getDeleteOptions();
		deleteOptions.setApiVersion("apps/v1");
		try {
		    try {
				coreV1Api.deleteNamespacedPod(name, namespace, TRUE, null, null, null, null, deleteOptions);
		    } catch (JsonSyntaxException e) {
			// K8s end exception ignore
		    } 
			List<String> podList = Lists.newArrayList();
			podList.add(name);
			if (wait) {
				waitForResourceDeletion(apiClient, podList, namespace, null);
			}
		} catch (ApiException e) {
			if (e.getCode() == 404) {
				return false;
			}
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE,
					ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.DELETE));
			LOGGER.error("Error while deleting Pod {} in namespace {} , error {}", name, namespace, ex.toString());
			throw ex;
		}
		return true;
	}

	@Override
	public boolean deleteBySelector(ApiClient apiClient, String selector, boolean label, String namespace, boolean wait)
			throws HyscaleException {
		try {
			List<V1Pod> V1PodList = getBySelector(apiClient, selector, label, namespace);
			if (V1PodList == null || V1PodList.isEmpty()) {
			}
			for (V1Pod V1Pod : V1PodList) {
				delete(apiClient, V1Pod.getMetadata().getName(), namespace, wait);
			}
		} catch (HyscaleException e) {
			if (DeployerErrorCodes.RESOURCE_NOT_FOUND.equals(e.getHyscaleErrorCode())) {
				LOGGER.error("Error while deleting Pods for selector {} in namespace {}, error {}", selector, namespace,
						e.toString());
				return false;
			}
			throw e;
		}
		return true;
	}

	@Override
	public String getKind() {
		return ResourceKind.POD.getKind();
	}

	public InputStream tailLogs(ApiClient apiClient, String name, String namespace, Integer readLines)
			throws HyscaleException {
		return tailLogs(apiClient, name, namespace, null, name, readLines);
	}
	
	public InputStream tailLogs(ApiClient apiClient, String serviceName, String namespace, String podName, String containerName, Integer readLines)
			throws HyscaleException {
		if(podName == null) {
			List<V1Pod> v1Pods = getBySelector(apiClient, ResourceLabelKey.SERVICE_NAME.getLabel() + "=" + serviceName, true,
					namespace);
			if (v1Pods == null || v1Pods.isEmpty()) {
				throw new HyscaleException(DeployerErrorCodes.FAILED_TO_RETRIEVE_POD, serviceName, namespace);
			}
			podName = v1Pods.get(0).getMetadata().getName();
		}
		OkHttpClient existingHttpClient = apiClient.getHttpClient();
		try {
		    // Update timeout
		    apiClient.setHttpClient(getUpdatedHttpClient(existingHttpClient));
			PodLogs logs = new PodLogs(apiClient);
			return logs.streamNamespacedPodLog(namespace, podName, containerName, null, readLines,
					true);
		} catch (IOException | ApiException e) {
			LOGGER.error("Failed to tail Pod logs for service {} in namespace {} ", serviceName, namespace);
			throw new HyscaleException(DeployerErrorCodes.FAILED_TO_TAIL_POD, serviceName, namespace);
		} finally {
		    apiClient.setHttpClient(existingHttpClient);
        }
	}
	
	private OkHttpClient getUpdatedHttpClient(OkHttpClient existingHttpClient) {
	    Builder newClientBuilder = existingHttpClient.newBuilder()
	            .readTimeout(120, TimeUnit.MINUTES);
	    
	    return newClientBuilder.build();
	}

	public InputStream getLogs(ApiClient apiClient, String name, String namespace, Integer readLines)
			throws HyscaleException {
		return getLogs(apiClient, name, namespace, null, name, readLines);
	}

	public InputStream getLogs(ApiClient apiClient, String serviceName, String namespace, String podName, String containerName, Integer readLines)
			throws HyscaleException {
		if(podName == null) {
			List<V1Pod> v1Pods = getBySelector(apiClient, ResourceLabelKey.SERVICE_NAME.getLabel() + "=" + serviceName, true,
					namespace);
			if (v1Pods == null || v1Pods.isEmpty()) {
				throw new HyscaleException(DeployerErrorCodes.FAILED_TO_RETRIEVE_POD, serviceName, namespace);
			}
			podName = v1Pods.get(0).getMetadata().getName();
		}
		try {
			CoreV1Api coreClient = new CoreV1Api(apiClient);
			Call call = coreClient.readNamespacedPodLogCall(podName, namespace, containerName, false, null, TRUE, false, null,
					readLines, true, null);
			Response response = call.execute();
			if (!response.isSuccessful()) {
				LOGGER.error("Failed to get Pod logs for service {} in namespace {} : {}", serviceName, namespace,
						response.body().string());
				throw new HyscaleException(DeployerErrorCodes.FAILED_TO_GET_LOGS, serviceName, namespace);
			}
			return response.body().byteStream();
		} catch (IOException | ApiException e) {
			LOGGER.error("Error while fetching Pod logs for service {} in namespace {} ", serviceName, namespace,
					e.getMessage());
			throw new HyscaleException(DeployerErrorCodes.FAILED_TO_GET_LOGS, serviceName, namespace);
		}
	}
	
    public List<V1Pod> getPodsForAllNamespaces(ApiClient apiClient) throws HyscaleException {
        return getPodsForAllNamespaces(apiClient, null, true);
    }

    public List<V1Pod> getPodsForAllNamespaces(ApiClient apiClient, String selector, boolean label)
            throws HyscaleException {
        CoreV1Api coreV1Api = new CoreV1Api(apiClient);
        String labelSelector = label ? selector : null;
        String fieldSelector = label ? null : selector;
        List<V1Pod> v1Pods = null;
        try {
            V1PodList v1PodList = coreV1Api.listPodForAllNamespaces(null, null, fieldSelector, labelSelector, null,
                    TRUE, null, null, null);
            v1Pods = v1PodList != null ? v1PodList.getItems() : null;
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_BY_SELECTOR);
            LOGGER.error("Error while listing Pods in all namespace, with selectors {},  error {}", selector,
                    ex.toString());
            throw ex;
        }
        return v1Pods;
    }

	// Integrate this check to K8sUtil
	private void waitForContainerCreation(ApiClient apiClient, V1Pod v1Pod, String name, String namespace) {
		long startTime = System.currentTimeMillis();
		boolean containerReady = false;
		WorkflowLogger.startActivity(DeployerActivity.WAITING_FOR_CONTAINER_CREATION);
		while (!containerReady && System.currentTimeMillis() - startTime < MAX_TIME_TO_CONTAINER_READY) {
			WorkflowLogger.continueActivity();
			try {
				v1Pod = get(apiClient, v1Pod.getMetadata().getName(), namespace);
				List<V1ContainerStatus> containerStatuses = v1Pod.getStatus().getContainerStatuses();
				if (containerStatuses != null && !containerStatuses.isEmpty()) {
					V1ContainerStatus v1ContainerStatus = v1Pod.getStatus().getContainerStatuses().stream()
							.filter(each -> {
								return each.getName().equals(name);
							}).findFirst().get();
					containerReady = v1ContainerStatus.getReady();
					// TODO Check if container is in error state and exit fast
				}
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
			} catch (HyscaleException ex) {

			}
		}

		if (containerReady) {
			WorkflowLogger.endActivity(Status.DONE);
		} else {
			WorkflowLogger.endActivity(Status.FAILED);
		}
	}

	@Override
	public boolean cleanUp() {
		return false;
	}

	@Override
	public int getWeight() {
	    return ResourceKind.POD.getWeight();
	}
	
	/**
     * It will fetch pod parent by using of servicename and namespace, based on pod parent it will get  latestpodselector.
     * Now it will start watching pod event changes on cluster based on namespace and latestpodselector.
     * While watching pods it is reading status, here we are mainly reading three status (initialization, creations and readiness),
     * after completion of each status we are showing status done message, whenever we get any pod has failed status immediately come out from watch
     * and show deployment fail message. 
     * 
     * @param apiClient
     * @param appName
     * @param serviceName
     * @param namespace
     */

	public void watch(ApiClient apiClient, String appName, String serviceName, String namespace)
			throws HyscaleException {
		 OkHttpClient httpClient=getUpdatedHttpClient(apiClient.getHttpClient());
		 apiClient.setHttpClient(httpClient);
		 CoreV1Api api = new CoreV1Api(apiClient);

		String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
		PodParent podParent=getPodParent(apiClient, serviceName, namespace);

		Integer replicas = null;
		String latestPodSelector = null;
		
		PodParentHandler podParentHandler = PodParentFactory.getHandler(podParent.getKind());
		latestPodSelector = podParentHandler.getPodSelector(apiClient, podParent.getParent(), selector);
		replicas = podParentHandler.getReplicas(podParent.getParent());
		
		if(latestPodSelector==null) {
			throw new HyscaleException(DeployerErrorCodes.POD_SELECTOR_NOT_FOUND);
		}
		Watch<V1Pod> watch = null;

		Set<String> readyPods = new HashSet<String>();
		Set<String> initializedPods = new HashSet<String>();
		Set<String> createdPods = new HashSet<String>();

		ActivityContext initializedActivityContext = new ActivityContext(DeployerActivity.POD_INITIALIZED);
		ActivityContext creationActivityContext = new ActivityContext(DeployerActivity.POD_CREATION);
		ActivityContext readyActivityContext = new ActivityContext(DeployerActivity.POD_READINESS);
		ActivityContext activityContext = null;

		boolean initializationActivityDone = false, creationActivityStarted = false, creationActivityDone = false,
				readyActivityStarted = false;
		
		try {
			watch = Watch.createWatch(apiClient,
					api.listNamespacedPodCall(namespace, null, false, null, null, latestPodSelector, null, null, replicas*POD_ACTIVITY_WAITING_TIME, Boolean.TRUE, null),
					new TypeToken<Watch.Response<V1Pod>>() {
					}.getType());
		} catch (ApiException e) {
			logger.error("Failed to watch pod events ",e);
			throw new HyscaleException(DeployerErrorCodes.FAILED_TO_RETRIEVE_POD);
		}
		try {
			WorkflowLogger.startActivity(initializedActivityContext);
			activityContext=initializedActivityContext;
			for (Watch.Response<V1Pod> item : watch) {
				WorkflowLogger.continueActivity(activityContext);
				/*if pod  status is failed then watch will exit*/
				if (PodPredicates.isPodFailed().test(item.object)) {
					break;
				}
				
				/*if pod restart count is reached to specified pod_restart_count then watch will exit*/
				if (PodPredicates.isPodRestarted().test(item.object, POD_RESTART_COUNT)) {
					break;
				}

				if (PodPredicates.isPodInitialized().test(item.object)) {
					initializedPods.add(item.object.getMetadata().getName());
					/*pod initialization activity completed*/
					if (initializedPods.size() == replicas && !initializationActivityDone) {
						initializationActivityDone = true;
						WorkflowLogger.endActivity(initializedActivityContext, Status.DONE);
					}
				}

				if (initializationActivityDone && !creationActivityStarted) {
					activityContext=creationActivityContext;
					WorkflowLogger.startActivity(creationActivityContext);
					creationActivityStarted = true;
				}
				if (PodPredicates.isPodCreated().test(item.object)) {
					createdPods.add(item.object.getMetadata().getName());
					/*pod creation activity completed*/
					if (createdPods.size() == replicas && !creationActivityDone) {
						WorkflowLogger.endActivity(creationActivityContext, Status.DONE);
						creationActivityDone = true;
					}
				}

				if (creationActivityDone && !readyActivityStarted) {
					activityContext=readyActivityContext;
					WorkflowLogger.startActivity(readyActivityContext);
					readyActivityStarted = true;
				}
				if (PodPredicates.isPodReady().test(item.object)) {
					readyPods.add(item.object.getMetadata().getName());
					/*pod readiness activity completed*/
					if (readyPods.size() == replicas) {
						WorkflowLogger.endActivity(readyActivityContext, Status.DONE);
						break;
					}
				}

				if (readyPods.size() == replicas) {
					break;
				}

			}
			
			if(!initializationActivityDone) {
				WorkflowLogger.endActivity(initializedActivityContext, Status.FAILED);
				throw new HyscaleException(DeployerErrorCodes.FAILED_TO_INITIALIZE_POD);
			}
			if(!creationActivityDone) {
				WorkflowLogger.endActivity(creationActivityContext, Status.FAILED);
				throw new HyscaleException(DeployerErrorCodes.FAILED_TO_CREATE_POD);
			}
			if(readyPods.size()!=replicas) {
				WorkflowLogger.endActivity(readyActivityContext, Status.FAILED);
				throw new HyscaleException(DeployerErrorCodes.POD_FAILED_READINESS);
			}
			
		} finally {
			try {
				watch.close();
			} catch (IOException e) {
				LOGGER.error("Error while watcing the service {} in namespace {}, error {}", serviceName, namespace, e);
			}
		}
	}


	/**
	 * It will read object(v1Deployment or v1StatefulSet) from cluster and return along with their resource kind
	 * 
	 * @param apiClient
	 * @param appName
	 * @param namespace
	 * @return podParent
	 */
	private PodParent getPodParent(ApiClient apiClient, String serviceName, String namespace) throws HyscaleException {
		V1StatefulSet v1StatefulSet = null;
		V1Deployment v1Deployment = null;
		PodParent podParent = new PodParent();
		try {
			V1StatefulSetHandler v1StatefulSetHandler = (V1StatefulSetHandler) ResourceHandlers
					.getHandlerOf(ResourceKind.STATEFUL_SET.getKind());
			v1StatefulSet = v1StatefulSetHandler.get(apiClient, serviceName, namespace);
			podParent.setKind(ResourceKind.STATEFUL_SET.getKind());
			podParent.setParent(v1StatefulSet);
		} catch (HyscaleException e) {
			if (!e.getHyscaleErrorCode().equals(DeployerErrorCodes.RESOURCE_NOT_FOUND)) {
				throw e;
			}
		}
		if (podParent.getKind() == null) {
			try {
				V1DeploymentHandler v1DeploymentHandler = (V1DeploymentHandler) ResourceHandlers
						.getHandlerOf(ResourceKind.DEPLOYMENT.getKind());
				v1Deployment = v1DeploymentHandler.get(apiClient, serviceName, namespace);
				podParent.setKind(ResourceKind.DEPLOYMENT.getKind());
				podParent.setParent(v1Deployment);
			} catch (HyscaleException e) {
				throw e;
			}
		}
		return podParent;
	}
	
	public boolean hasPodParent(ApiClient apiClient, String selector, String namespace) {
	    PodParent podParent = null;
	    try {
            podParent = getPodParent(apiClient, selector, namespace);
        } catch (HyscaleException e) {
        }
	    return podParent == null ? false : true;
	}
}
