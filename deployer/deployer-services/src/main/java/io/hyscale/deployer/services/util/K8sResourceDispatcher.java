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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.deployer.services.broker.K8sResourceBroker;
import io.hyscale.deployer.services.builder.NamespaceBuilder;
import io.hyscale.deployer.services.client.GenericK8sClient;
import io.hyscale.deployer.services.client.K8sResourceClient;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.manager.AnnotationsUpdateManager;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.model.DeployerActivity;
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
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Namespace;

/**
 * Handles generic resource level operation such as apply, undeploy among others
 */
public class K8sResourceDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(K8sResourceDispatcher.class);

    private K8sResourceBroker resourceBroker;
    private ApiClient apiClient;
    private String namespace;
    private boolean waitForReadiness;

    public K8sResourceDispatcher(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.namespace = K8SRuntimeConstants.DEFAULT_NAMESPACE;
        this.waitForReadiness = true;
        this.resourceBroker = new K8sResourceBroker(apiClient);
    }

    public K8sResourceDispatcher withNamespace(String namespace) {
        this.namespace = namespace;
        this.resourceBroker.withNamespace(namespace);
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

        Map<String, CustomObject> kindVsCustomObject = getCustomObjects(manifests);
        List<KubernetesResource> k8sResources = getSortedResources(manifests);

        for (KubernetesResource k8sResource : k8sResources) {
            AnnotationsUpdateManager.update(k8sResource, AnnotationKey.LAST_UPDATED_AT,
                    DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            ResourceLifeCycleHandler lifeCycleHandler = ResourceHandlers.getHandlerOf(k8sResource.getKind());
            if (lifeCycleHandler != null && k8sResource.getResource() != null && k8sResource.getV1ObjectMeta() != null) {
                try {
                    String name = k8sResource.getV1ObjectMeta().getName();
                    if (resourceBroker.get(lifeCycleHandler, name) != null) {
                        resourceBroker.update(lifeCycleHandler, k8sResource, lifeCycleHandler.getUpdatePolicy());
                    } else {
                        resourceBroker.create(lifeCycleHandler, k8sResource.getResource());
                    }
                    kindVsCustomObject.remove(k8sResource.getKind());
                } catch (HyscaleException ex) {
                    logger.error("Failed to apply resource :{} Reason :: {}", k8sResource.getKind(), ex.getMessage(),ex);
                }
            }
        }
        if(kindVsCustomObject != null && !kindVsCustomObject.isEmpty()){
            kindVsCustomObject.forEach((kind,object)->{
                // Using Generic K8s Client
                GenericK8sClient genericK8sClient = new K8sResourceClient(apiClient).
                        withNamespace(namespace).forKind(object);
                if(genericK8sClient != null){
                    try{
                        if(genericK8sClient.get(object) == null){
                            logger.debug("Creating resource with Generic client for Kind - "+kind);
                            genericK8sClient.create(object);
                        }else{
                            //TODO Update resource
                        }
                    }catch (HyscaleException ex){
                        logger.error("Failed to apply resource :{} Reason :: {}", kind, ex.getMessage());
                    }
                }
            });
        }
    }

    private Map<String,CustomObject> getCustomObjects(List<Manifest> manifests) throws HyscaleException {
        Map<String,CustomObject> kindVsObject = new HashMap<>();

                for (Manifest manifest : manifests) {
                try {
                    CustomObject object = KubernetesResourceUtil.getK8sCustomObjectResource(manifest,namespace);
                    if(object != null){
                        logger.debug("Adding kind - "+object.getKind());
                        kindVsObject.put(object.getKind(),object);
                    }
                } catch (Exception e) {
                    HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_APPLY_MANIFEST);
                    logger.error("Error while applying manifests to kubernetes", ex);
                    throw ex;
                }
            }
        return kindVsObject;
    }
    
    private List<KubernetesResource> getSortedResources(List<Manifest> manifests) throws HyscaleException{
        List<KubernetesResource> k8sResources = new ArrayList<>();
        
        for (Manifest manifest : manifests) {
            try {
                KubernetesResource kubernetesResource = KubernetesResourceUtil.getKubernetesResource(manifest, namespace);
                if(kubernetesResource != null){
                        k8sResources.add(kubernetesResource);
                }
            } catch (Exception e) {
                HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_APPLY_MANIFEST);
                logger.error("Error while applying manifests to kubernetes", ex);
                throw ex;
            }
        }
        // Sort resources to deploy secrets and configmaps before Pod Controller
        k8sResources.sort((resource1, resource2) -> ResourceKind.fromString(resource1.getKind()).getWeight()
                - ResourceKind.fromString(resource2.getKind()).getWeight());
        
        return k8sResources;
    }

    /**
     * Creates namespace if it doesnot exist on the cluster
     *
     * @throws HyscaleException
     */
    private void createNamespaceIfNotExists() throws HyscaleException {
        NamespaceHandler namespaceHandler = (NamespaceHandler) ResourceHandlers.getHandlerOf(ResourceKind.NAMESPACE.getKind());
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
        List<String> failedResources = new ArrayList<>();

        String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
        List<ResourceLifeCycleHandler> handlersList = ResourceHandlers.getAllHandlers();
        if (handlersList == null) {
            return;
        }
        // Sort handlers based on weight
        List<ResourceLifeCycleHandler> sortedHandlersList = handlersList.stream()
                .sorted((handler1, handler2) -> handler1.getWeight() - handler2.getWeight())
                .collect(Collectors.toList());
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
                    resourceDeleted = resourceDeleted || result;
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
