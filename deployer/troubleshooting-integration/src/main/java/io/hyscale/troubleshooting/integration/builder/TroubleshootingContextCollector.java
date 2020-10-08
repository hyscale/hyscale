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
package io.hyscale.troubleshooting.integration.builder;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.models.ResourceFieldSelectorKey;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.commons.utils.FieldSelectorUtil;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.handler.impl.V1DeploymentHandler;
import io.hyscale.deployer.services.handler.impl.V1EventHandler;
import io.hyscale.deployer.services.handler.impl.V1StorageClassHandler;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.deployer.services.util.KubernetesResourceUtil;
import io.hyscale.troubleshooting.integration.errors.TroubleshootErrorCodes;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.hyscale.troubleshooting.integration.spring.TroubleshootingConfig;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;


//TODO JAVADOC
@Component
public class TroubleshootingContextCollector {

    private static final Logger logger = LoggerFactory.getLogger(TroubleshootingContextCollector.class);

    @Autowired
    private K8sClientProvider k8sClientProvider;

    @Autowired
    private TroubleshootingConfig troubleshootingConfig;
    
    private List<String> troubleshootResources = Arrays.asList(ResourceKind.STATEFUL_SET.getKind(), ResourceKind.DEPLOYMENT.getKind(),
            ResourceKind.REPLICA_SET.getKind(), ResourceKind.POD.getKind(), ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());

    public TroubleshootingContext build(@NonNull ServiceMetadata serviceMetadata, @NonNull K8sAuthorisation k8sAuthorisation, @NonNull String namespace) throws HyscaleException {
        TroubleshootingContext context = new TroubleshootingContext();
        try {
            ApiClient apiClient = k8sClientProvider.get(k8sAuthorisation);

            context.setServiceMetadata(serviceMetadata);
            context.setTrace(troubleshootingConfig.isTrace());
            long start = System.currentTimeMillis();
            context.setResourceInfos(filter(getResources(serviceMetadata, apiClient, namespace)));
            if (context.isTrace()) {
                logger.debug("Time taken to build the context for service {} is {}", serviceMetadata.getServiceName(), System.currentTimeMillis() - start);
            }
        } catch (HyscaleException e) {
            logger.error("Error while preparing context to troubleshoot the service {}", serviceMetadata.getServiceName());
            throw e;
        }
        return context;
    }

    // Get only the latest or current deployed pods
    private Map<String, List<TroubleshootingContext.ResourceInfo>> filter(Map<String, List<TroubleshootingContext.ResourceInfo>> resources) {
        if (resources == null || resources.isEmpty()) {
            return resources;
        }

        /*  Incase of statefulset deployments , pods are always upto date
            with the deployment because it does RollingUpdate in the reverse order.
            During deployment update with patch if any pod is in failing state pods are
            delete and recreated so statefulset always contain the latest deployed pods*/

        List<TroubleshootingContext.ResourceInfo> statefulSetResourceInfos = resources.get(ResourceKind.STATEFUL_SET.getKind());
        List<TroubleshootingContext.ResourceInfo> deploymentResourceInfos = resources.get(ResourceKind.DEPLOYMENT.getKind());
        if (statefulSetResourceInfos != null && !statefulSetResourceInfos.isEmpty()) {
            return resources;
        }
        if (deploymentResourceInfos != null && !deploymentResourceInfos.isEmpty()) {
            List<TroubleshootingContext.ResourceInfo> replicaSetResourceInfos = resources.get(ResourceKind.REPLICA_SET.getKind());
            List<TroubleshootingContext.ResourceInfo> podResourceInfos = resources.get(ResourceKind.POD.getKind());
            List<TroubleshootingContext.ResourceInfo> filteredPodResourceInfos = new ArrayList<>();
            deploymentResourceInfos.stream().filter(each -> each != null && each.getResource() instanceof V1Deployment).forEach(each -> {
                String deploymentRevision = V1DeploymentHandler.getDeploymentRevision((V1Deployment) each.getResource());
                if (StringUtils.isNotBlank(deploymentRevision)) {
                    V1ReplicaSet replicaSet = filterReplicaSetByrevision(replicaSetResourceInfos, deploymentRevision);
                    if (replicaSet != null) {
                        String podTemplateHash = replicaSet.getMetadata().getLabels().get(K8SRuntimeConstants.K8S_DEPLOYMENT_POD_TEMPLATE_HASH);
                        filteredPodResourceInfos.addAll(filterPodsByHash(podResourceInfos, podTemplateHash));
                    }
                }
            });
            resources.remove(ResourceKind.POD.getKind());
            resources.put(ResourceKind.POD.getKind(), filteredPodResourceInfos);
            return resources;
        }
        return resources;
    }

    private Collection<? extends TroubleshootingContext.ResourceInfo> filterPodsByHash(List<TroubleshootingContext.ResourceInfo> podResourceInfos, String podTemplateHash) {
        List<TroubleshootingContext.ResourceInfo> result = new ArrayList<>();
        if ((podResourceInfos == null || podResourceInfos.isEmpty()) || StringUtils.isBlank(podTemplateHash)) {
            return result;
        }
        for (TroubleshootingContext.ResourceInfo podResource : podResourceInfos) {
            if (podResource != null && podResource.getResource() != null) {
                V1Pod pod = (V1Pod) podResource.getResource();
                if (pod.getMetadata().getLabels().get(K8SRuntimeConstants.K8S_DEPLOYMENT_POD_TEMPLATE_HASH).equals(podTemplateHash)) {
                    result.add(podResource);
                }
            }
        }
        return result;
    }

    private V1ReplicaSet filterReplicaSetByrevision(List<TroubleshootingContext.ResourceInfo> replicaSetResourceInfos, String deploymentRevision) {
        if (replicaSetResourceInfos == null || replicaSetResourceInfos.isEmpty()) {
            return null;
        }
        V1ReplicaSet replicaSet = null;
        for (TroubleshootingContext.ResourceInfo eachReplicaSet : replicaSetResourceInfos) {
            replicaSet = (V1ReplicaSet) eachReplicaSet.getResource();
            if (replicaSet != null && deploymentRevision.equals(replicaSet.getMetadata().getAnnotations().
                    get(AnnotationKey.K8S_DEPLOYMENT_REVISION.getAnnotation()))) {
                break;
            }
        }
        return replicaSet;
    }

    private Map<String, List<TroubleshootingContext.ResourceInfo>> getResources(@NonNull ServiceMetadata serviceMetadata, @NonNull ApiClient apiClient, @NonNull String namespace) throws HyscaleException {
        String selector = ResourceSelectorUtil.getSelector(serviceMetadata.getAppName(), serviceMetadata.getEnvName(), serviceMetadata.getServiceName());
        List<ResourceLifeCycleHandler> handlerList = getResourceHandlers();
        if (handlerList == null || handlerList.isEmpty()) {
            logger.error("Error while fetching resource lifecycle handler ");
            throw new HyscaleException(TroubleshootErrorCodes.ERROR_WHILE_BUILDING_RESOURCES);
        }

        V1EventHandler eventHandler = (V1EventHandler) ResourceHandlers.getHandlerOf(ResourceKind.EVENT.getKind());
        Map<String, List<TroubleshootingContext.ResourceInfo>> resourceMap = new HashMap<>();
        handlerList.stream().forEach(each -> {
            List resourceList = null;
            try {
                resourceList = each.getBySelector(apiClient, selector, true, namespace);
            } catch (HyscaleException e) {
                logger.debug("Error while fetching resource {} in namespace {} of selector {}", each.getKind(), namespace, selector, e);
            }

            // Construct resourceInfo for each resource of this kind
            if (resourceList != null && !resourceList.isEmpty()) {
                List<TroubleshootingContext.ResourceInfo> resourceInfoList = new ArrayList<>();
                resourceList.stream().forEach(eachResource -> {
                    TroubleshootingContext.ResourceInfo resourceInfo = new TroubleshootingContext.ResourceInfo();
                    try {
                        V1ObjectMeta v1ObjectMeta = KubernetesResourceUtil.getObjectMeta(eachResource);
                        resourceInfo.setResource(eachResource);
                        resourceInfo.setEvents(eventHandler.getBySelector(apiClient, getFieldSelector(v1ObjectMeta.getName(), namespace), false, namespace));
                    } catch (HyscaleException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        logger.debug("Error while fetching resource {} logs in namespace {}", eachResource.getClass(), namespace);
                    }
                    resourceInfoList.add(resourceInfo);
                });

                if (!resourceInfoList.isEmpty()) {
                    resourceMap.put(each.getKind(), resourceInfoList);
                }
            }
        });

        // Adding storage class to the context
        V1StorageClassHandler storageClassHandler = (V1StorageClassHandler) ResourceHandlers.getHandlerOf(ResourceKind.STORAGE_CLASS.getKind());
        List<V1StorageClass> storageClasses = storageClassHandler.getAll(apiClient);
        if (storageClasses != null && !storageClasses.isEmpty()) {
            List<TroubleshootingContext.ResourceInfo> storageClassResourceInfoList =
                    storageClasses.stream().map(each -> {
                        TroubleshootingContext.ResourceInfo resourceInfo = new TroubleshootingContext.ResourceInfo();
                        resourceInfo.setResource(each);
                        return resourceInfo;
                    }).collect(Collectors.toList());
            if (storageClassResourceInfoList != null && !storageClassResourceInfoList.isEmpty()) {
                resourceMap.put(ResourceKind.STORAGE_CLASS.getKind(), storageClassResourceInfoList);
            }
        }

        return resourceMap;
    }
    
    private List<ResourceLifeCycleHandler> getResourceHandlers() {
        return ResourceHandlers.getAllHandlers().stream()
                .filter(each -> troubleshootResources.contains(each.getKind())).collect(Collectors.toList());
    }

    private String getFieldSelector(String name, String namespace) {
        Map<ResourceFieldSelectorKey, String> fieldMap = new HashMap<>();
        fieldMap.put(V1EventHandler.EventFieldKey.INVOLVED_OBJECT_NAME, name);
        fieldMap.put(V1EventHandler.EventFieldKey.INVOLVED_OBJECT_NAMESPACE, namespace);
        return FieldSelectorUtil.getSelectorFromFieldMap(fieldMap);
    }
}
