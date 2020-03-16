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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.utils.ResourceLabelUtil;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.AppMetadata;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.impl.V1DeploymentHandler;
import io.hyscale.deployer.services.handler.impl.V1ReplicaSetHandler;
import io.hyscale.deployer.services.handler.impl.V1StatefulSetHandler;
import io.hyscale.deployer.services.predicates.PodPredicates;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1ReplicaSet;

public class K8sDeployerUtil {

    private static final Logger logger = LoggerFactory.getLogger(K8sDeployerUtil.class);

    /**
     * Get {@link V1Deployment} from cluster based on namespace, appname and service name.
     * Deployment provides the revision for corresponding replica set
     * Get {@link V1ReplicaSet} from cluster based on namespace, appname, service name and revision.
     * Replica set provides pod-template-hash label(cluster internal) for corresponding pods
     * From the provided pods return the ones which contains pod-template-hash in label 
     * 
     * @param apiClient
     * @param appName
     * @param serviceName
     * @param namespace
     * @param podList
     * @return pods from pod list which refer to deployment for the app and service in namespace
     */
    public static List<V1Pod> filterPodsByDeployment(ApiClient apiClient, String appName, String serviceName,
            String namespace, List<V1Pod> podList) {
        String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
        V1DeploymentHandler v1DeploymentHandler = (V1DeploymentHandler) ResourceHandlers
                .getHandlerOf(ResourceKind.DEPLOYMENT.getKind());
        List<V1Deployment> deploymentList = null;
        try {
            deploymentList = v1DeploymentHandler.getBySelector(apiClient, selector, true, namespace);
        } catch (HyscaleException e) {
            logger.error("Error fetching deployment for pod filtering, ignoring", e);
            return podList;
        }
        if (deploymentList == null || deploymentList.isEmpty()) {
            logger.debug("No deployment found for filtering pods, returning empty list");
            return new ArrayList<V1Pod>();
        }

        String revision = V1DeploymentHandler.getDeploymentRevision(deploymentList.get(0));

        if (StringUtils.isBlank(revision)) {
            return podList;
        }
        V1ReplicaSetHandler v1ReplicaSetHandler = (V1ReplicaSetHandler) ResourceHandlers
                .getHandlerOf(ResourceKind.REPLICA_SET.getKind());

        V1ReplicaSet replicaSet = null;

        try {
            replicaSet = v1ReplicaSetHandler.getReplicaSetByRevision(apiClient, namespace, selector, true, revision);
        } catch (HyscaleException e) {
            logger.error("Error fetching replica set with revision {} for pod filtering, ignoring", revision, e);
            return podList;
        }
        if (replicaSet == null) {
            logger.debug("No Replica set found with revision: {} for filtering pods, returning empty list", revision);
            return new ArrayList<V1Pod>();
        }

        Map<String, String> replicaLabels = replicaSet.getMetadata().getLabels();
        String podTemplateHash = replicaLabels != null
                ? replicaLabels.get(K8SRuntimeConstants.K8s_DEPLOYMENT_POD_TEMPLATE_HASH)
                : null;

        Map<String, String> searchLabel = new HashMap<String, String>();
        searchLabel.put(K8SRuntimeConstants.K8s_DEPLOYMENT_POD_TEMPLATE_HASH, podTemplateHash);

        return K8sPodUtil.filterPods(podList, PodPredicates.podContainsLabel(), searchLabel);

    }
    
    /**
     * Fetch service status from pod parent, called in case pods are not present
     * @param context
     * @return Not running status if pod owner found else not deployed(when service name provided)
     */
    public static List<DeploymentStatus> getOwnerDeploymentStatus(ApiClient apiClient, DeploymentContext context) {
        if (context == null) {
            return null;
        }
        logger.debug("No pods found, getting status from pods owner");
        String serviceName = context.getServiceName();
        String namespace = context.getNamespace();
        String selector = ResourceSelectorUtil.getServiceSelector(context.getAppName(), serviceName);
        
        Map<String, DeploymentStatus> serviceVsDeploymentStatus = new HashMap<String, DeploymentStatus>();
        // Deployment
        V1DeploymentHandler deploymentHandler = (V1DeploymentHandler)ResourceHandlers
                .getHandlerOf(ResourceKind.DEPLOYMENT.getKind());
        List<DeploymentStatus> deploymentStatus = deploymentHandler.getStatus(apiClient, selector, true, namespace);
        if (deploymentStatus != null && !deploymentStatus.isEmpty()) {
            logger.debug("Getting status from Deployments");
            deploymentStatus.stream().forEach(status -> {
                serviceVsDeploymentStatus.put(status.getServiceName(), status);
            });
        }
        // StatefulSet
        V1StatefulSetHandler stsHandler = (V1StatefulSetHandler)ResourceHandlers
                .getHandlerOf(ResourceKind.STATEFUL_SET.getKind());
        
        deploymentStatus = stsHandler.getStatus(apiClient, selector, true, namespace);
        if (deploymentStatus != null && !deploymentStatus.isEmpty()) {
            logger.debug("Getting status from StatefulSet");
            deploymentStatus.stream().forEach(status -> {
                serviceVsDeploymentStatus.put(status.getServiceName(), status);
            });
        }
        if (serviceVsDeploymentStatus.isEmpty()) {
            return serviceName != null ? Arrays.asList(DeploymentStatusUtil.getNotDeployedStatus(serviceName)): null;
        }
        
        return new ArrayList(serviceVsDeploymentStatus.values());
    }
    
    /**
     * Get list of services deployed for the given app
     * Fetch it from owners instead of pods as pods might not be created in some cases
     * @param apiClient
     * @param context
     * @return List of deployed services
     * @throws HyscaleException 
     */
    public static Set<String> getDeployedServices(ApiClient apiClient, DeploymentContext context) throws HyscaleException {
        if (context == null) {
            return null;
        }
        Set<String> services = new HashSet<String>();
        if (StringUtils.isNotBlank(context.getServiceName())) {
            services.add(context.getServiceName());
            return services;
        }
        String namespace = context.getNamespace();
        String selector = ResourceSelectorUtil.getSelector(context.getAppName());

        // StatefulSet
        V1StatefulSetHandler stsHandler = (V1StatefulSetHandler) ResourceHandlers
                .getHandlerOf(ResourceKind.STATEFUL_SET.getKind());

        services.addAll(stsHandler.getServiceNames(apiClient, selector, true, namespace));
        // Deployment
        V1DeploymentHandler deploymentHandler = (V1DeploymentHandler) ResourceHandlers
                .getHandlerOf(ResourceKind.DEPLOYMENT.getKind());
        services.addAll(deploymentHandler.getServiceNames(apiClient, selector, true, namespace));

        logger.debug("Found services {} for app {}", services, context.getAppName());
        return services;
    }
    
    /**
     * Gets app and service name from labels generated by hyscale
     * For pods not deployed with hyscale AppMetadata with only namespace will be returned
     * @param podList
     * @return List of {@link AppMetadata} containing details of deployed apps
     */
    public static List<AppMetadata> getAppMetadata(List<V1Pod> podList){
        if (podList == null) {
            return null;
        }
        Map<String, Map<String, Set<String>>> namespaceVsAppsVsServices = getNamespaceVsAppsVsServices(podList);
        
        List<AppMetadata> appsMetadata = new ArrayList<AppMetadata>();
        
        namespaceVsAppsVsServices.entrySet().stream().forEach(namespaceVsApps -> {
            String namespace = namespaceVsApps.getKey();
            
            if (namespaceVsApps.getValue() == null || namespaceVsApps.getValue().isEmpty()) {
                AppMetadata appMetadata = new AppMetadata();
                appMetadata.setNamespace(namespace);
                appsMetadata.add(appMetadata);
                return;
            }
            namespaceVsApps.getValue().entrySet().forEach( appsVsServices ->{
                AppMetadata appMetadata = new AppMetadata();
                appMetadata.setNamespace(namespace);
                appMetadata.setAppName(appsVsServices.getKey());
                appMetadata.setServices(new ArrayList<String>(appsVsServices.getValue()));
                appsMetadata.add(appMetadata);
            });
        });
        return appsMetadata;
    }

    private static Map<String, Map<String, Set<String>>> getNamespaceVsAppsVsServices(List<V1Pod> pods) {
        Map<String, Map<String, Set<String>>> mapping = new HashMap<String, Map<String,Set<String>>>();
        
        pods.forEach(pod -> {
            String namespace = pod.getMetadata().getNamespace();
            String appName = ResourceLabelUtil.getAppName(pod.getMetadata().getLabels());
            String serviceName = ResourceLabelUtil.getServiceName(pod.getMetadata().getLabels());
            
            if(mapping.get(namespace) == null) {
                mapping.put(namespace, new HashMap<String, Set<String>>());
            }
            if (StringUtils.isBlank(appName)) {
                return;
            }
            if (mapping.get(namespace).get(appName) == null) {
                mapping.get(namespace).put(appName, new HashSet<String>());
            }
            if (StringUtils.isBlank(serviceName)) {
                return;
            }
            mapping.get(namespace).get(appName).add(serviceName);
        });
        return mapping;
    }
    
}
