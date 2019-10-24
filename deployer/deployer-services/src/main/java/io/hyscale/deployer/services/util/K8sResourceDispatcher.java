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
package io.hyscale.deployer.services.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.hyscale.deployer.services.builder.NamespaceBuilder;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.model.ResourceUpdatePolicy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.KubernetesResource;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.handler.impl.NamespaceHandler;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1ObjectMeta;

/**
 * Handles generic resource level operation such as apply, undeploy among others
 *
 */
public class K8sResourceDispatcher {

	private static final Logger logger = LoggerFactory.getLogger(K8sResourceDispatcher.class);

	private ResourceUpdatePolicy updatePolicy;
	private ApiClient apiClient;
	private String namespace;
	private boolean waitForReadiness;

	public K8sResourceDispatcher() {
		this.namespace = K8SRuntimeConstants.DEFAULT_NAMESPACE;
		this.waitForReadiness = true;
		this.updatePolicy = ResourceUpdatePolicy.PATCH;
	}

	public K8sResourceDispatcher withClient(ApiClient apiClient) {
		this.apiClient = apiClient;
		return this;
	}

	public K8sResourceDispatcher withNamespace(String namespace) {
		this.namespace = namespace;
		return this;
	}

	public K8sResourceDispatcher withUpdatePolicy(ResourceUpdatePolicy updatePolicy) {
		this.updatePolicy = updatePolicy;
		return this;
	}

	public void create(List<Manifest> manifests) throws HyscaleException {
		apply(manifests);
	}

	public boolean isWaitForReadiness() {
		return waitForReadiness;
	}

	public void waitForReadiness(boolean waitForReadiness) {
		this.waitForReadiness = waitForReadiness;
	}

	/**
	 * Applies manifest to cluster
	 * Use update policy if resource found on cluster otherwise create
	 * @param manifests
	 * @throws HyscaleException
	 */
	public void apply(List<Manifest> manifests) throws HyscaleException {
		if (manifests == null || manifests.isEmpty()) {
			logger.error("Found empty manifests to deploy ");
			throw new HyscaleException(DeployerErrorCodes.MANIFEST_REQUIRED);
		}
		createNamespaceIfNotExists();
		for (Manifest manifest : manifests) {
			try {
			    KubernetesResource k8sResource = KubernetesResourceUtil.getKubernetesResource(manifest, namespace);
				ResourceLifeCycleHandler lifeCycleHandler = ResourceHandlers.getHandlerOf(k8sResource.getKind());
				if (lifeCycleHandler != null && k8sResource != null && k8sResource.getResource() != null && k8sResource.getV1ObjectMeta() != null) {
					try {
						String name = k8sResource.getV1ObjectMeta().getName();
						if (lifeCycleHandler.get(apiClient, name , namespace) != null) {
							handleResourceUpdate(lifeCycleHandler, k8sResource.getV1ObjectMeta(), k8sResource.getResource());
						} else {
							lifeCycleHandler.create(apiClient, k8sResource.getResource(), namespace);
						}
					} catch (HyscaleException ex) {
						lifeCycleHandler.create(apiClient, k8sResource.getResource(), namespace);
					}
					
				}
			} catch (HyscaleException e) {
				logger.error("Error while applying manifests to kubernetes", e);
				throw e;
			} catch (Exception e) {
				HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_APPLY_MANIFEST);
				logger.error("Error while applying manifests to kubernetes", ex);
				throw ex;
			}
		}
	}
	
	/**
	 * Creates namespace if it doesnot exist on the cluster
	 * @throws HyscaleException
	 */
	private void createNamespaceIfNotExists() throws HyscaleException {
		ResourceLifeCycleHandler resourceHandler = ResourceHandlers.getHandlerOf(ResourceKind.NAMESPACE.getKind());
		if (resourceHandler == null) {
		    return ;
		}
		NamespaceHandler namespaceHandler = (NamespaceHandler) resourceHandler;
		V1Namespace v1Namespace = null;
		try {
		    v1Namespace = namespaceHandler.get(apiClient, namespace, null);
		} catch (HyscaleException ex) {
		    logger.error("Error while getting namespace: {}, error: {}", namespace, ex.getMessage());
		}
		if (v1Namespace == null) {
		    logger.debug("Namespace: {}, does not exist, creating", namespace);
		    namespaceHandler.create(apiClient, NamespaceBuilder.build(namespace), namespace);
		}
	}

	/**
	 * Handle resource update based on update policy
	 */
	private void handleResourceUpdate(ResourceLifeCycleHandler lifeCycleHandler, V1ObjectMeta objectMeta, Object obj)
			throws NoSuchMethodException, HyscaleException {
		String namespace = objectMeta.getNamespace();
		switch (updatePolicy) {
		case PATCH:
			try {
				boolean isPatched = lifeCycleHandler.patch(apiClient, objectMeta.getName(), namespace, obj);
				if (!isPatched) {
					// Fallback to delete and create if patch fails to apply
					lifeCycleHandler.delete(apiClient, objectMeta.getName(), namespace, true);
					lifeCycleHandler.create(apiClient, obj, namespace);

				}
			} catch (HyscaleException e) {
				// Fallback to delete and create if patch fails to apply
				lifeCycleHandler.delete(apiClient, objectMeta.getName(), namespace, true);
				lifeCycleHandler.create(apiClient, obj, namespace);
			}
			break;
		case UPDATE:
			lifeCycleHandler.update(apiClient, obj, namespace);
			break;
		case DELETE_AND_CREATE:
			lifeCycleHandler.delete(apiClient, objectMeta.getName(), namespace, true);
			lifeCycleHandler.create(apiClient, obj, namespace);
			break;
		}
	}

	public void undeployApp(String appName) throws HyscaleException {
		undeploy(appName, null);
	}

	public void undeployService(String appName, String serviceName) throws HyscaleException {
		undeploy(appName, serviceName);
	}

	/**
	 * Undeploy resources from cluster
	 * deletes all resources for which clean up is enabled based on appName and serviceName
	 * @param appName
	 * @param serviceName
	 * @throws HyscaleException if failed to delete any resource
	 */
	public void undeploy(String appName, String serviceName) throws HyscaleException {
		if (StringUtils.isBlank(appName)) {
			logger.error("No applicaton found for undeployment");
			throw new HyscaleException(DeployerErrorCodes.APPLICATION_REQUIRED);
		}
		List<String> failedResources = new ArrayList<String>();

		String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
		List<ResourceLifeCycleHandler> handlersList = ResourceHandlers.getHandlersList();
		if (handlersList == null) {
			return;
		}
		// Sort handlers based on weight
		List<ResourceLifeCycleHandler> sortedHandlersList = handlersList.stream().sorted((ResourceLifeCycleHandler handler1, ResourceLifeCycleHandler handler2) -> {
			return handler1.getWeight() - handler2.getWeight();
		}).collect(Collectors.toList());
		boolean resourceDeleted = false;
		for (ResourceLifeCycleHandler lifeCycleHandler : sortedHandlersList) {
			if (lifeCycleHandler == null) {
				continue;
			}
			String resourceKind = lifeCycleHandler.getKind();
			if (lifeCycleHandler.cleanUp()) {
				try {
					boolean result = lifeCycleHandler.deleteBySelector(apiClient, selector, true, namespace, true);
					resourceDeleted = resourceDeleted ? true : result;
				} catch (HyscaleException ex) {
					failedResources.add(resourceKind);
				}

			}
		}
		if (!failedResources.isEmpty()) {
			String[] args = new String[failedResources.size()];
			failedResources.toArray(args);
			throw new HyscaleException(DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE, args);
		}
		if (!resourceDeleted) {
		    WorkflowLogger.info(DeployerActivity.NO_RESOURCES_TO_UNDEPLOY);
		}
	}

}
