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

import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.utils.TimeStampProvider;
import io.hyscale.deployer.services.broker.K8sResourceBroker;
import io.hyscale.deployer.services.builder.NamespaceBuilder;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.manager.AnnotationsUpdateManager;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.model.ResourceUpdatePolicy;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
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

/**
 * Handles generic resource level operation such as apply, undeploy among others
 */
public class K8sResourceDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(K8sResourceDispatcher.class);

    private ResourceUpdatePolicy updatePolicy;
    private K8sResourceBroker resourceBroker;
    private ApiClient apiClient;
    private String namespace;
    private boolean waitForReadiness;

    public K8sResourceDispatcher(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.namespace = K8SRuntimeConstants.DEFAULT_NAMESPACE;
        this.waitForReadiness = true;
        this.updatePolicy = ResourceUpdatePolicy.PATCH;
        this.resourceBroker = new K8sResourceBroker(apiClient);
    }

    public K8sResourceDispatcher withNamespace(String namespace) {
        this.namespace = namespace;
        this.resourceBroker.withNamespace(namespace);
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
     *
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
                AnnotationsUpdateManager.update(k8sResource, AnnotationKey.LAST_UPDATED_AT.LAST_UPDATED_AT,
                        DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
                ResourceLifeCycleHandler lifeCycleHandler = ResourceHandlers.getHandlerOf(k8sResource.getKind());
                if (lifeCycleHandler != null && k8sResource != null && k8sResource.getResource() != null && k8sResource.getV1ObjectMeta() != null) {
                    try {
                        String name = k8sResource.getV1ObjectMeta().getName();
                        if (resourceBroker.get(lifeCycleHandler, name) != null) {
                            resourceBroker.update(lifeCycleHandler, k8sResource, updatePolicy);
                        } else {
                            resourceBroker.create(lifeCycleHandler, k8sResource.getResource());
                        }
                    } catch (HyscaleException ex) {
                        logger.error("Failed to apply resource :{} Reason :: {}", k8sResource.getKind(), ex.getMessage(), ex);
                    }
                }
            } catch (Exception e) {
                HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_APPLY_MANIFEST);
                logger.error("Error while applying manifests to kubernetes", ex);
                throw ex;
            }
        }
    }

    /**
     * Creates namespace if it doesnot exist on the cluster
     *
     * @throws HyscaleException
     */
    private void createNamespaceIfNotExists() throws HyscaleException {
        ResourceLifeCycleHandler resourceHandler = ResourceHandlers.getHandlerOf(ResourceKind.NAMESPACE.getKind());
        if (resourceHandler == null) {
            return;
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
     * Undeploy resources from cluster
     * Deletes all resources for which clean up is enabled based on appName and serviceName
     *
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
                    logger.debug("Undeployment status for resource {} is {}", resourceKind, result);
                    resourceDeleted = resourceDeleted ? true : result;
                } catch (HyscaleException ex) {
                    logger.error("Failed to undeploy resource {}", resourceKind, ex);
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
