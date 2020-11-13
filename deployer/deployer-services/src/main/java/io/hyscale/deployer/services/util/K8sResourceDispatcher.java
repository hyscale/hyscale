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

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.KubernetesResource;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.broker.K8sResourceBroker;
import io.hyscale.deployer.services.builder.NamespaceBuilder;
import io.hyscale.deployer.services.client.GenericK8sClient;
import io.hyscale.deployer.services.client.K8sResourceClient;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.handler.impl.NamespaceHandler;
import io.hyscale.deployer.services.manager.AnnotationsUpdateManager;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Namespace;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

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

        List<String> appliedKinds = new ArrayList<>();
        appliedKinds.addAll(kindVsCustomObject.keySet());
        logger.debug("applied resource kinds - {}",appliedKinds);

        for (KubernetesResource k8sResource : k8sResources) {
            AnnotationsUpdateManager.update(k8sResource, AnnotationKey.LAST_UPDATED_AT,
                    DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            if(k8sResource.getKind().equalsIgnoreCase("deployment") || k8sResource.getKind().equalsIgnoreCase("statefulset")){
                AnnotationsUpdateManager.update(k8sResource,AnnotationKey.HYSCALE_APPLIED_KINDS,appliedKinds.toString());
            }
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
        applyCustomResources(kindVsCustomObject);
    }

    private void applyCustomResources(Map<String, CustomObject> kindVsCustomObject){
        if(kindVsCustomObject != null && !kindVsCustomObject.isEmpty()){
            kindVsCustomObject.forEach((kind,object)->{
                // Using Generic K8s Client
                GenericK8sClient genericK8sClient = new K8sResourceClient(apiClient).
                        withNamespace(namespace).forKind(ResourceKind.fromString(kind));
                if(genericK8sClient != null){
                    try{
                        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING,kind);
                        if(genericK8sClient.get(object) != null){
                            logger.debug("Updating resource with Generic client for Kind - {}",kind);
                            if(!genericK8sClient.patch(object)){
                                // Delete and Create if failed to Patch
                                if(genericK8sClient.delete(object)) {
                                    genericK8sClient.create(object);
                                    WorkflowLogger.endActivity(Status.DONE);
                                }
                            }
                        }else{
                            logger.debug("Creating resource with Generic client for Kind - {}",kind);
                            genericK8sClient.create(object);
                            WorkflowLogger.endActivity(Status.DONE);
                        }
                    }catch (HyscaleException ex){
                        WorkflowLogger.endActivity(Status.FAILED);
                        logger.error("Failed to apply resource :{} Reason :: {}", kind, ex.getMessage());
                    }
                }
            });
        }
    }


    private MultiValueMap<String,String> getAppliedKindsMapForServices(List<CustomObject> workloadResources){
        MultiValueMap<String,String> serviceVsAppliedKinds = new LinkedMultiValueMap();
        if(workloadResources != null && !workloadResources.isEmpty()){
            workloadResources.stream().filter(Objects::nonNull).forEach((resource)->{
                if(resource.getMetadata() != null){
                    String name = resource.getMetadata().getName();
                    Map<String,String> annotations = resource.getMetadata().getAnnotations();
                    String appliedKindsStr = annotations.get(AnnotationKey.HYSCALE_APPLIED_KINDS.getAnnotation());
                    List<String> appliedKindsList = Arrays.asList(appliedKindsStr.substring(1,appliedKindsStr.length()-1).replaceAll("\\s","").split(","));
                    serviceVsAppliedKinds.put(name,appliedKindsList);
                }
            });
            return serviceVsAppliedKinds;
        }
        return null;
    }

        private void deleteResources(String labelSelector, List<String> appliedKindsList) throws HyscaleException {
        boolean resourcesDeleted = true;

        List<String> failedResources = new ArrayList<>();
        if(appliedKindsList!=null && !appliedKindsList.isEmpty()){
            for(String kind : appliedKindsList){
                logger.info("Cleaning up - {}",kind);
                ResourceKind resourceKind = ResourceKind.fromString(kind);
                GenericK8sClient genericK8sClient = new K8sResourceClient(apiClient).
                        withNamespace(namespace).forKind(resourceKind);
                List<CustomObject> resources =  genericK8sClient.getBySelector(labelSelector);
                if(resources == null || resources.isEmpty()){
                    continue;
                }
                WorkflowLogger.startActivity(DeployerActivity.DELETING,kind);
                for(CustomObject resource : resources){
                    resource.put("kind",kind);
                    boolean result = genericK8sClient.delete(resource);
                    logger.debug("Undeployment status for resource {} is {}", kind, result);
                    resourcesDeleted = resourcesDeleted && result;
                }
                if(resourcesDeleted){ WorkflowLogger.endActivity(Status.DONE); }
                else{
                    failedResources.add(kind);
                    WorkflowLogger.endActivity(Status.FAILED);
                }
            }
        }else if(!failedResources.isEmpty()){
            String[] args = new String[failedResources.size()];
            failedResources.toArray(args);
            throw new HyscaleException(DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE, args);
        }else{
            WorkflowLogger.info(DeployerActivity.NO_RESOURCES_TO_UNDEPLOY);
        }
    }

    /**
     * Undeploy resources from cluster
     * Deletes all resources belonging to all services in an app environment
     * @param appName
     * @throws HyscaleException
     */
    public void undeployApp(String appName) throws HyscaleException {
        logger.info("Undeploy initiated for application - {}",appName);
        if (StringUtils.isBlank(appName)) {
            logger.error("No applicaton found for undeployment");
            throw new HyscaleException(DeployerErrorCodes.APPLICATION_REQUIRED);
        }
        List<CustomObject> workloadResources = new ArrayList<>();
        GenericK8sClient genericK8sClient = new K8sResourceClient(apiClient).
                withNamespace(namespace).forKind(ResourceKind.DEPLOYMENT);
        workloadResources.addAll(genericK8sClient.getAll());

        genericK8sClient = new K8sResourceClient(apiClient).
                withNamespace(namespace).forKind(ResourceKind.STATEFUL_SET);
        workloadResources.addAll(genericK8sClient.getAll());

        MultiValueMap<String,String> appliedKindsMapForServices = getAppliedKindsMapForServices(workloadResources);

        if(appliedKindsMapForServices != null && !appliedKindsMapForServices.isEmpty()){
            for (Map.Entry<String, List<String>> entry : appliedKindsMapForServices.entrySet()) {
                List<String> appliedKindsList = entry.getValue();
                String selector = ResourceSelectorUtil.getServiceSelector(appName,null);
                deleteResources(selector, appliedKindsList);
            }
        }
    }

    /**
     * Undeploy resources from cluster
     * Deletes all resources in a service
     *
     * @param appName
     * @param serviceName
     * @throws HyscaleException if failed to delete any resource
     */
    public void undeployService(String appName, String serviceName) throws HyscaleException {
        logger.info("Undeploy initiated for service - {}",serviceName);
        if (StringUtils.isBlank(appName)) {
            logger.error("No applicaton found for undeployment");
            throw new HyscaleException(DeployerErrorCodes.APPLICATION_REQUIRED);
        }
        List<CustomObject> workloadResources = new ArrayList<>();
        List<String> appliedKindsList = null;
        GenericK8sClient genericK8sClient = new K8sResourceClient(apiClient).
                withNamespace(namespace).forKind(ResourceKind.DEPLOYMENT);
        workloadResources.add(genericK8sClient.getResourceByName(serviceName));
        genericK8sClient = new K8sResourceClient(apiClient).
                withNamespace(namespace).forKind(ResourceKind.STATEFUL_SET);
        workloadResources.add(genericK8sClient.getResourceByName(serviceName));
        MultiValueMap<String,String> appliedKindsMapForServices = getAppliedKindsMapForServices(workloadResources);
        if(appliedKindsMapForServices != null){
            appliedKindsList = appliedKindsMapForServices.get(serviceName);
        }
        String selector = ResourceSelectorUtil.getServiceSelector(appName,serviceName);
        deleteResources(selector,appliedKindsList);
    }

    private Map<String,CustomObject> getCustomObjects(List<Manifest> manifests) throws HyscaleException {
        Map<String,CustomObject> kindVsObject = new HashMap<>();

                for (Manifest manifest : manifests) {
                try {
                    CustomObject object = KubernetesResourceUtil.getK8sCustomObjectResource(manifest,namespace);
                    if(object != null){
                        logger.debug("Adding kind - {}",object.getKind());
                        kindVsObject.put(object.getKind(),object);
                    }
                } catch (Exception e) {
                    HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_APPLY_MANIFEST);
                    logger.error("Error while applying manifests to kubernetes {}", ex);
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
                logger.error("Error while applying manifests to kubernetes {}", ex);
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

}
