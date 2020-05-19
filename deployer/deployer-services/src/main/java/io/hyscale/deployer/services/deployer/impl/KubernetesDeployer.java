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
package io.hyscale.deployer.services.deployer.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.hyscale.deployer.services.manager.ScaleServiceManager;
import io.hyscale.deployer.services.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.models.KubernetesResource;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.YAMLManifest;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.AppMetadata;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.builder.AppMetadataBuilder;
import io.hyscale.deployer.services.builder.PodBuilder;
import io.hyscale.deployer.services.config.DeployerConfig;
import io.hyscale.deployer.services.deployer.Deployer;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.factory.PodParentFactory;
import io.hyscale.deployer.services.handler.AuthenticationHandler;
import io.hyscale.deployer.services.handler.PodParentHandler;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.handler.impl.V1PersistentVolumeClaimHandler;
import io.hyscale.deployer.services.handler.impl.V1PodHandler;
import io.hyscale.deployer.services.handler.impl.V1ServiceHandler;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.deployer.services.util.DeploymentStatusUtil;
import io.hyscale.deployer.services.util.K8sDeployerUtil;
import io.hyscale.deployer.services.util.K8sPodUtil;
import io.hyscale.deployer.services.util.K8sReplicaUtil;
import io.hyscale.deployer.services.util.K8sResourceDispatcher;
import io.hyscale.deployer.services.util.KubernetesResourceUtil;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;

/**
 * {@link Deployer} implementation for K8s Cluster
 */
@Component
public class KubernetesDeployer implements Deployer<K8sAuthorisation> {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesDeployer.class);
    private static final String STORAGE = "storage";

    @Autowired
    private DeployerConfig deployerConfig;

    @Autowired
    private K8sClientProvider clientProvider;

    @Autowired
    private AppMetadataBuilder appMetadataBuilder;

    @Autowired
    private AuthenticationHandler<K8sAuthorisation> authenticationHandler;

    @Autowired
    private ScaleServiceManager scaleServiceManager;

    @Override
    public void deploy(DeploymentContext context) throws HyscaleException {

        K8sResourceDispatcher resourceDispatcher = new K8sResourceDispatcher(clientProvider.get((K8sAuthorisation) context.getAuthConfig()));
        try {
            resourceDispatcher.waitForReadiness(context.isWaitForReadiness());
            resourceDispatcher.withNamespace(context.getNamespace()).apply(context.getManifests());

        } catch (HyscaleException e) {
            logger.error("Error while deploying service {} in namespace {} , error {} ", context.getServiceName(),
                    context.getNamespace(), e.toString());
            throw e;
        }
    }

    /**
     * Wait for Pod scheduled, creation and Readiness state
     */
    @Override
    public void waitForDeployment(DeploymentContext context) throws HyscaleException {
        ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());
        V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        String serviceName = context.getServiceName();
        String namespace = context.getNamespace();
        String appName = context.getAppName();
        WorkflowLogger.header(DeployerActivity.WAITING_FOR_DEPLOYMENT);
        try {
            podHandler.watch(apiClient, appName, serviceName, namespace);
        } catch (HyscaleException e) {
            throw e;
        }
    }

    @Override
    public ResourceStatus status(String namespace, Manifest manifest, K8sAuthorisation authConfig) throws Exception {
        /*
         * For each resource kind and name, using resource handler Get resource from
         * cluster Get status of the fetched resource
         */
        ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfig);
        YAMLManifest yamlManifest = (YAMLManifest) manifest;
        try {
            KubernetesResource resource = KubernetesResourceUtil.getKubernetesResource(yamlManifest, namespace);
            String kind = resource.getKind();
            ResourceLifeCycleHandler lifeCycleHandler = ResourceHandlers.getHandlerOf(kind);
            String resourceName = resource.getV1ObjectMeta().getName();

            return lifeCycleHandler.status(lifeCycleHandler.get(apiClient, resourceName, namespace));
        } catch (Exception e) {
            logger.error("Error while preparing client, error {} ", e.toString());
            throw e;
        }
    }

    @Override
    public List<Pod> getPods(String namespace, String appName, String serviceName, K8sAuthorisation k8sAuthorisation) throws Exception {
        List<Pod> podList = new ArrayList<Pod>();
        List<V1Pod> v1PodList = null;
        try {
            ApiClient apiClient = clientProvider.get(k8sAuthorisation);
            V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());

            String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);

            v1PodList = v1PodHandler.getBySelector(apiClient, selector, true, namespace);
            if (v1PodList == null || v1PodList.isEmpty()) {
                return null;
            }

            for (V1Pod v1Pod : v1PodList) {
                PodBuilder builder = new PodBuilder();
                List<V1Volume> v1VolumeList = v1Pod.getSpec().getVolumes();
                List<Volume> podVolumeList = new ArrayList<Volume>();
                Set<String> volumeNames = new HashSet<>();
                if (v1VolumeList != null) {
                    for (V1Volume v1Volume : v1VolumeList) {
                        Volume volume = getVolume(apiClient, v1Volume, namespace);
                        if (volume != null) {
                            volumeNames.add(volume.getName());
                            podVolumeList.add(volume);
                        }
                    }
                }
                builder.withName(v1Pod.getMetadata().getName())
                        .withStatus(K8sPodUtil.getAggregatedStatusOfContainersForPod(v1Pod))
                        .withContainers(getContainers(v1Pod, volumeNames))
                        .withVolumes(podVolumeList)
                        .withReady(K8sPodUtil.checkForPodCondition(v1Pod, PodCondition.READY));
                podList.add(builder.get());
            }
            return podList;
        } catch (HyscaleException e) {
            logger.error("Error while getting Pods, error {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void unDeploy(DeploymentContext context) throws HyscaleException {
        K8sResourceDispatcher resourceDispatcher = new K8sResourceDispatcher(clientProvider.get((K8sAuthorisation) context.getAuthConfig()));
        try {
            resourceDispatcher.withNamespace(context.getNamespace()).undeploy(context.getAppName(),
                    context.getServiceName());
        } catch (HyscaleException e) {
            logger.error("Error while undeploying service in namespace {} , error {} ", context.getNamespace(),
                    e.toString());
            throw e;
        }
    }


    @Override
    public boolean authenticate(K8sAuthorisation authConfig) throws HyscaleException {
        return authenticationHandler.authenticate(authConfig);
    }

    @Override
    public InputStream logs(K8sAuthorisation authConfig, String serviceName, String namespace, String podName,
                            String containerName, Integer readLines, boolean tail) throws HyscaleException {
        try {
            ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfig);
            V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
            if (readLines == null) {
                readLines = deployerConfig.getDefaultTailLines();
            }
            if (tail) {
                return podHandler.tailLogs(apiClient, serviceName, namespace, podName, containerName, readLines);
            } else {
                return podHandler.getLogs(apiClient, serviceName, namespace, podName, containerName, readLines);
            }
        } catch (HyscaleException e) {
            logger.error("Error while tailing logs, error {} ", e.toString());
            throw e;
        }
    }

    @Override
    public ServiceAddress getServiceAddress(DeploymentContext context) throws HyscaleException {
        ServiceAddress serviceAddress = null;
        try {
            ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());
            V1ServiceHandler v1ServiceHandler = (V1ServiceHandler) ResourceHandlers
                    .getHandlerOf(ResourceKind.SERVICE.getKind());
            serviceAddress = v1ServiceHandler.getServiceAddress(apiClient, context.getServiceName(),
                    context.getNamespace(), context.isWaitForReadiness());
        } catch (HyscaleException e) {
            logger.error("Error while preparing client, error {} ", e.toString());
            throw e;
        }
        return serviceAddress;
    }


    @Override
    public DeploymentStatus getServiceDeploymentStatus(DeploymentContext context) throws HyscaleException {
        String serviceName = context.getServiceName();
        if (StringUtils.isBlank(serviceName)) {
            throw new HyscaleException(DeployerErrorCodes.SERVICE_REQUIRED);
        }
        String selector = ResourceSelectorUtil.getServiceSelector(context.getAppName(), serviceName);
        V1PodHandler podHandler = (V1PodHandler)ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        PodParent podParent = null;
        try {
            ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());
            podParent = podHandler.getPodParent(apiClient, selector, context.getNamespace());
        } catch (HyscaleException e) {
            logger.error("Error while fetching status {} ", e);
            throw e;
        }
        
        if (podParent == null) {
            return DeploymentStatusUtil.getNotDeployedStatus(serviceName);
        }
        PodParentHandler podParentHandler = PodParentFactory.getHandler(podParent.getKind());
        return updateServiceAddress(context, podParentHandler.buildStatus(podParent.getParent()));
    }
    
    @Override
    public List<DeploymentStatus> getDeploymentStatus(DeploymentContext context) throws HyscaleException {
        List<PodParent> podParentList = null;
        String selector = ResourceSelectorUtil.getSelector(context.getAppName());
        V1PodHandler podHandler = (V1PodHandler)ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        try {
            ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());
            podParentList = podHandler.getPodParentsList(apiClient, selector, context.getNamespace());
        } catch (HyscaleException e) {
            logger.error("Error while fetching status {} ", e);
            throw e;

        }
        if (podParentList == null || podParentList.isEmpty()) {
            return null;
        }
        List<DeploymentStatus> deploymentStatusList =  podParentList.stream().map(each -> {
            PodParentHandler podParentHandler = PodParentFactory.getHandler(each.getKind());
            return podParentHandler.buildStatus(each.getParent());
          }).collect(Collectors.toList());
        return deploymentStatusList.stream().map(each -> updateServiceAddress(context, each)).collect(Collectors.toList());
    }
    
    private DeploymentStatus updateServiceAddress(DeploymentContext context, DeploymentStatus deploymentStatus) {
        context.setServiceName(deploymentStatus.getServiceName());
        try {
            ServiceAddress serviceAddress = getServiceAddress(context);
            if (serviceAddress != null) {
                deploymentStatus.setServiceAddress(serviceAddress.toString());
            }
        } catch (HyscaleException e) {
            logger.debug("Failed to get service address {} ", e.getHyscaleErrorCode());
            deploymentStatus.setServiceAddress("Failed to get service address, try again");
        }
        return deploymentStatus;
    }

    /**
     * Get Replica info for pods
     *
     * @param authConfig cluster auth configuration
     *                   Implementation:
     *                   <p>
     *                   isFilter if disabled return replica info for all pods fetched based on selector
     *                   if enabled,
     *                   1. If owner kind for pods is different, warn user and return replica info for all pods
     *                   2. If owner kind is {@link ResourceKind #DEPLOYMENT} or {@link ResourceKind #REPLICA_SET},
     *                   Get Revision from deployment, get replicas set with this revision,
     *                   filter pods based on pod-template-hash from replica set
     *                   return replica info for filtered pods
     *                   3. Else return replica info for all pods
     *                   </p>
     */
    @Override
    public List<ReplicaInfo> getReplicas(K8sAuthorisation authConfig, String appName, String serviceName, String namespace,
                                         boolean all) throws HyscaleException {
        return getReplicasBySelector(authConfig, namespace, ResourceSelectorUtil.getServiceSelector(appName, serviceName));
    }

    @Override
    public List<AppMetadata> getAppsMetadata(K8sAuthorisation authConfig) throws HyscaleException {
        ApiClient apiClient = clientProvider.get(authConfig);
        V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        return appMetadataBuilder.build(v1PodHandler.getPodsForAllNamespaces(apiClient));
    }

    @Override
    public List<ReplicaInfo> getLatestReplicas(K8sAuthorisation authConfig, String appName, String serviceName, String namespace) throws HyscaleException {
        ApiClient client = clientProvider.get(authConfig);
        List<V1Pod> latestPods = K8sDeployerUtil.getLatestPods(client, appName, serviceName, namespace);
        return K8sReplicaUtil.getReplicaInfo(latestPods);
    }

    private List<ReplicaInfo> getReplicasBySelector(K8sAuthorisation authorisation, String namespace, String selector) throws HyscaleException {
        List<V1Pod> v1PodList = null;
        ApiClient apiClient = clientProvider.get(authorisation);
        V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());

        v1PodList = v1PodHandler.getBySelector(apiClient, selector, true, namespace);
        if (v1PodList == null || v1PodList.isEmpty()) {
            return null;
        }
        return K8sReplicaUtil.getReplicaInfo(v1PodList);
    }

    private List<Container> getContainers(V1Pod v1Pod, Set<String> volumeNames) {
        List<Container> containers = new ArrayList<Container>();
        // get all status
        Map<String, String> containerVsStatus = new HashMap<>();
        if (v1Pod.getStatus() != null && v1Pod.getStatus().getContainerStatuses() != null) {
            v1Pod.getStatus().getContainerStatuses().forEach((containerStatus) -> {
                String status = K8sPodUtil.getContainerStatus(containerStatus);
                if (status != null) {
                    containerVsStatus.put(containerStatus.getName(), status);
                }
            });
        }
        v1Pod.getSpec().getContainers().forEach((v1Container) -> {
            Container container = new Container();
            container.setName(v1Container.getName());
            container.setStatus(containerVsStatus.get(v1Container.getName()));
            if (v1Container.getVolumeMounts() != null) {
                List<VolumeMount> volumeMounts = v1Container.getVolumeMounts().stream()
                        .filter(v1VolumeMount -> volumeNames.contains(v1VolumeMount.getName()))
                        .map(this::getVolumeMount).collect(Collectors.toList());
                container.setVolumeMounts(volumeMounts);
            }
            containers.add(container);
        });
        return containers;
    }

    private Volume getVolume(ApiClient apiClient, V1Volume v1Volume, String namespace) throws HyscaleException {
        if (v1Volume.getPersistentVolumeClaim() == null) {
            return null;
        }
        String claimName = v1Volume.getPersistentVolumeClaim().getClaimName();
        if (claimName == null) {
            return null;
        }
        V1PersistentVolumeClaimHandler v1PersistentVolumeClaimHandler = (V1PersistentVolumeClaimHandler) ResourceHandlers
                .getHandlerOf(ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());
        Volume volume = new Volume();
        volume.setName(v1Volume.getName());
        volume.setClaimName(claimName);

        V1PersistentVolumeClaim pvc = v1PersistentVolumeClaimHandler.get(apiClient, claimName,
                namespace);
        if (pvc.getStatus() != null && pvc.getStatus().getCapacity() != null
                && pvc.getStatus().getCapacity().get(STORAGE) != null) {
            volume.setSize(pvc.getStatus().getCapacity().get(STORAGE).toSuffixedString());
        }
        return volume;
    }

    private VolumeMount getVolumeMount(V1VolumeMount v1VolumeMount) {
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setMountPath(v1VolumeMount.getMountPath());
        volumeMount.setName(v1VolumeMount.getName());
        if (v1VolumeMount.getReadOnly() != null) {
            volumeMount.setReadOnly(v1VolumeMount.getReadOnly());
        }
        return volumeMount;
    }

    @Override
    public ScaleStatus scale(K8sAuthorisation authConfig, String appName, String serviceName, String namespace, ScaleSpec scaleSpec) throws HyscaleException {
        ApiClient apiClient = clientProvider.get(authConfig);
        return scaleServiceManager.scale(apiClient, appName, serviceName, namespace, scaleSpec);
    }

}
