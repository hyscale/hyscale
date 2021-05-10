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
import io.hyscale.deployer.core.model.CustomResourceKind;
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
import io.hyscale.deployer.services.model.PodParent;
import io.hyscale.deployer.services.processor.PodParentUtil;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Namespace;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles generic resource level operation such as apply, undeploy among others
 */

public class K8sResourceDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(K8sResourceDispatcher.class);

    private static final String APPLY_MANIFEST_ERROR = "Error while applying manifests to kubernetes";

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
        List<String> appliedKinds = new ArrayList<>();
        List<CustomObject> customObjects = new ArrayList<>();
        List<KubernetesResource> kubernetesResourceList = new ArrayList<>();
        try {
            for (Manifest manifest : manifests) {
                CustomObject customObject = KubernetesResourceUtil.getK8sCustomObjectResource(manifest, namespace);
                appliedKinds.add(buildAppliedKindAnnotation(customObject));
                if (ResourceHandlers.getHandlerOf(customObject.getKind()) != null) {
                    kubernetesResourceList.add(KubernetesResourceUtil.getKubernetesResource(manifest, namespace));
                } else {
                    customObjects.add(customObject);
                }
            }
            applyK8sResources(kubernetesResourceList, appliedKinds);
            applyCustomResources(customObjects);
            logger.info("Successfully applied all Manifests");

        } catch (Exception e) {
            HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_APPLY_MANIFEST);
            logger.error(APPLY_MANIFEST_ERROR, ex);
            throw ex;
        }
    }

    public void applyK8sResources(List<KubernetesResource> k8sResources, List<String> appliedKinds) {
        for (KubernetesResource k8sResource : k8sResources) {
            AnnotationsUpdateManager.update(k8sResource, AnnotationKey.LAST_UPDATED_AT,
                    DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            updateAndApplyResource(k8sResource, appliedKinds);
        }
    }

    public void updateAndApplyResource(KubernetesResource k8sResource, List<String> appliedKinds) {
        ResourceLifeCycleHandler lifeCycleHandler = ResourceHandlers.getHandlerOf(k8sResource.getKind());
        if (lifeCycleHandler != null) {
            if (lifeCycleHandler.isWorkLoad()) {
                AnnotationsUpdateManager.update(k8sResource, AnnotationKey.HYSCALE_APPLIED_KINDS,
                        appliedKinds.toString());
            }
            if (k8sResource.getResource() != null && k8sResource.getV1ObjectMeta() != null) {
                try {
                    String name = k8sResource.getV1ObjectMeta().getName();
                    if (resourceBroker.get(lifeCycleHandler, name) != null) {
                        resourceBroker.update(lifeCycleHandler, k8sResource, lifeCycleHandler.getUpdatePolicy());
                    } else {
                        resourceBroker.create(lifeCycleHandler, k8sResource.getResource());
                    }
                } catch (HyscaleException ex) {
                    logger.error("Failed to apply resource :{} Reason :: {}", k8sResource.getKind(), ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Undeploy resources from cluster
     * Deletes all resources belonging to all services in an app environment
     *
     * @param appName
     * @throws HyscaleException
     */
    public void undeployApp(String appName) throws HyscaleException {
        logger.info("Undeploy initiated for application - {}", appName);
        if (StringUtils.isBlank(appName)) {
            logger.error("No applicaton found for undeployment");
            throw new HyscaleException(DeployerErrorCodes.APPLICATION_REQUIRED);
        }
        PodParentUtil podParentUtil = new PodParentUtil(apiClient, namespace);
        Map<String, PodParent> serviceVsPodParents = podParentUtil.getServiceVsPodParentMap(appName);
        if (serviceVsPodParents != null && !serviceVsPodParents.isEmpty()) {
            for (Map.Entry<String, PodParent> entry : serviceVsPodParents.entrySet()) {
                PodParent podParent = entry.getValue();
                List<CustomResourceKind> appliedKindsList = podParentUtil.getAppliedKindsList(podParent);
                String selector = ResourceSelectorUtil.getServiceSelector(appName, null);
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
        logger.info("Undeploy initiated for service - {}", serviceName);
        if (StringUtils.isBlank(appName)) {
            logger.error("No applicaton found for undeployment");
            throw new HyscaleException(DeployerErrorCodes.APPLICATION_REQUIRED);
        }
        PodParentUtil podParentUtil = new PodParentUtil(apiClient, namespace);
        PodParent podParent = podParentUtil.getPodParentForService(serviceName);
        List<CustomResourceKind> appliedKindsList = podParentUtil.getAppliedKindsList(podParent);
        String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
        deleteResources(selector, appliedKindsList);
    }

    private String buildAppliedKindAnnotation(CustomObject customObject) {
        return customObject.getKind() + ":" + customObject.getApiVersion();
    }

    private void applyCustomResources(List<CustomObject> customObjectList) {
        // Using Generic K8s Client
        customObjectList.stream().forEach(object -> {
            GenericK8sClient genericK8sClient = new K8sResourceClient(apiClient).
                    withNamespace(namespace).forKind(new CustomResourceKind(object.getKind(), object.getApiVersion()));
            try {
                WorkflowLogger.startActivity(DeployerActivity.DEPLOYING, object.getKind());
                if (genericK8sClient.get(object) != null && !genericK8sClient.patch(object)) {
                    logger.debug("Updating resource with Generic client for Kind - {}", object.getKind());
                    // Delete and Create if failed to Patch
                    logger.info("Deleting & Creating resource : {}", object.getKind());
                    genericK8sClient.delete(object);
                } else {
                    logger.debug("Creating resource with Generic client for Kind - {}", object.getKind());
                }
                genericK8sClient.create(object);
                WorkflowLogger.endActivity(Status.DONE);
            } catch (HyscaleException ex) {
                WorkflowLogger.endActivity(Status.FAILED);
                logger.error("Failed to apply resource :{} Reason :: {}", object.getKind(), ex.getMessage());
            }
        });
    }

    private void deleteResources(String labelSelector, List<CustomResourceKind> appliedKindsList) throws HyscaleException {
        boolean resourcesDeleted = true;

        List<String> failedResources = new ArrayList<>();
        if (appliedKindsList != null && !appliedKindsList.isEmpty()) {
            for (CustomResourceKind customResource : appliedKindsList) {
                logger.info("Cleaning up - {}", customResource.getKind());
                GenericK8sClient genericK8sClient = new K8sResourceClient(apiClient).
                        withNamespace(namespace).forKind(customResource);
                List<CustomObject> resources = genericK8sClient.getBySelector(labelSelector);
                if (resources == null || resources.isEmpty()) {
                    continue;
                }
                WorkflowLogger.startActivity(DeployerActivity.DELETING, customResource.getKind());
                for (CustomObject resource : resources) {
                    resource.put("kind", customResource.getKind());
                    boolean result = genericK8sClient.delete(resource);
                    logger.debug("Undeployment status for resource {} is {}", customResource.getKind(), result);
                    resourcesDeleted = resourcesDeleted && result;
                }
                if (resourcesDeleted) {
                    WorkflowLogger.endActivity(Status.DONE);
                } else {
                    failedResources.add(customResource.getKind());
                    WorkflowLogger.endActivity(Status.FAILED);
                }
            }
        } else if (!failedResources.isEmpty()) {
            String[] args = new String[failedResources.size()];
            failedResources.toArray(args);
            throw new HyscaleException(DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE, args);
        } else {
            WorkflowLogger.info(DeployerActivity.NO_RESOURCES_TO_UNDEPLOY);
        }
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
