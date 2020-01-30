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
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.hyscale.deployer.services.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.ActivityContext;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.models.KubernetesResource;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.ResourceLabelKey;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.models.YAMLManifest;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.commons.utils.ThreadPoolUtil;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.core.model.ReplicaInfo;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.config.DeployerConfig;
import io.hyscale.deployer.services.deployer.Deployer;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.handler.impl.V1PersistentVolumeClaimHandler;
import io.hyscale.deployer.services.handler.impl.V1PodHandler;
import io.hyscale.deployer.services.handler.impl.V1ServiceHandler;
import io.hyscale.deployer.services.predicates.PodPredicates;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.deployer.services.util.DeploymentStatusUtil;
import io.hyscale.deployer.services.util.K8sPodUtil;
import io.hyscale.deployer.services.util.K8sResourceDispatcher;
import io.hyscale.deployer.services.util.KubernetesResourceUtil;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1Volume;
import io.kubernetes.client.models.V1VolumeMount;

/**
 * {@link Deployer} implementation for K8s Cluster
 */
@Component
public class KubernetesDeployer implements Deployer {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesDeployer.class);
    private static final long DELTA_WAIT_MILLIS = 5000;
    private static final long MAX_POD_WAIT_SLEEP_INTERVAL_IN_MILLIS = 3000;
    private static final String STORAGE = "storage";

    @Autowired
    private DeployerConfig deployerConfig;

    @Autowired
    private K8sClientProvider clientProvider;

    @Override
    public void deploy(DeploymentContext context) throws HyscaleException {

        K8sResourceDispatcher resourceDispatcher = new K8sResourceDispatcher(clientProvider.get((K8sAuthorisation) context.getAuthConfig()));
        try {
            resourceDispatcher.waitForReadiness(context.isWaitForReadiness());
            resourceDispatcher.withNamespace(context.getNamespace()).withUpdatePolicy(deployerConfig.getUpdatePolicy())
                    .apply(context.getManifests());

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

        String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
        deltaWait(DELTA_WAIT_MILLIS);
        // Pod Scheduling
        ActivityContext podInitContext = new ActivityContext(DeployerActivity.POD_INITIALIZED);
        waitForPodActivity(apiClient, podHandler, namespace, selector, PodPredicates.isPodschedulingCondition(),
                podInitContext);

        // Pod Creation
        ActivityContext podCreationContext = new ActivityContext(DeployerActivity.POD_CREATION);
        waitForPodActivity(apiClient, podHandler, namespace, selector, PodPredicates.isPodCreated(),
                podCreationContext);
        // Pod readiness
        ActivityContext podReadinessContext = new ActivityContext(DeployerActivity.POD_READINESS);
        waitForPodActivity(apiClient, podHandler, namespace, selector, PodPredicates.isPodReady(), podReadinessContext);
    }

    @Override
    public ResourceStatus status(String namespace, Manifest manifest, AuthConfig authConfig) throws Exception {
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
    public boolean authenticate(AuthConfig authConfig) {
        // TODO
        return false;
    }

    @Override
    public InputStream logs(DeploymentContext deploymentContext) throws HyscaleException {
        String serviceName = deploymentContext.getServiceName();
        String namespace = deploymentContext.getNamespace();
        Integer readLines = deploymentContext.getReadLines();
        return logs(deploymentContext.getAuthConfig(), serviceName, namespace, null, serviceName, readLines,
                deploymentContext.isTailLogs());
    }

    @Override
    public InputStream logs(AuthConfig authConfig, String serviceName, String namespace, String podName,
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
        if (context == null) {
            throw new HyscaleException(DeployerErrorCodes.APPLICATION_REQUIRED);
        }

        if (context != null && StringUtils.isBlank(context.getServiceName())) {
            throw new HyscaleException(DeployerErrorCodes.SERVICE_REQUIRED);
        }

        List<V1Pod> v1PodList = null;
        DeploymentStatus deploymentStatus = new DeploymentStatus();
        deploymentStatus.setServiceName(context.getServiceName());
        try {
            ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());
            V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
            String selector = ResourceSelectorUtil.getServiceSelector(context.getAppName(), context.getServiceName());
            v1PodList = v1PodHandler.getBySelector(apiClient, selector, true, context.getNamespace());
            deploymentStatus = getPodDeploymentStatus(context, v1PodList);
        } catch (HyscaleException e) {
            return DeploymentStatusUtil.getNotDeployedStatus(context.getServiceName());
        }
        return deploymentStatus;
    }

    /**
     * Get Status for services based on app label
     */
    @Override
    public List<DeploymentStatus> getDeploymentStatus(DeploymentContext context) throws HyscaleException {
        List<DeploymentStatus> deploymentStatusList = new ArrayList<>();
        List<V1Pod> v1PodList = null;
        try {
            ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());
            V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
            String selector = ResourceSelectorUtil.getServiceSelector(context.getAppName(), context.getServiceName());
            v1PodList = v1PodHandler.getBySelector(apiClient, selector, true, context.getNamespace());
            if (v1PodList == null || v1PodList.isEmpty()) {
                // Assuming no service deployed
                return deploymentStatusList;
            }
            Map<String, List<V1Pod>> servicePodsMap = new HashMap<String, List<V1Pod>>();

            v1PodList.stream().forEach(v1Pod -> {
                String serviceName = v1Pod.getMetadata().getLabels().get(ResourceLabelKey.SERVICE_NAME.getLabel());
                if (servicePodsMap.get(serviceName) == null) {
                    servicePodsMap.put(serviceName, new ArrayList<V1Pod>());
                }
                servicePodsMap.get(serviceName).add(v1Pod);
            });

            servicePodsMap.entrySet().stream().forEach(each -> {
                context.setServiceName(each.getKey());
                deploymentStatusList.add(getPodDeploymentStatus(context, each.getValue()));
            });
        } catch (HyscaleException e) {
            logger.error("Error while fetching status {} ", e);
            throw e;

        }
        return deploymentStatusList;
    }

    private DeploymentStatus getPodDeploymentStatus(DeploymentContext context, List<V1Pod> v1PodList) {

        if (v1PodList == null || v1PodList.isEmpty()) {
            return DeploymentStatusUtil.getNotDeployedStatus(context.getServiceName());
        }
        DeploymentStatus deploymentStatus = new DeploymentStatus();
        deploymentStatus.setAge(DeploymentStatusUtil.getAge(v1PodList));
        deploymentStatus.setMessage(DeploymentStatusUtil.getMessage(v1PodList));
        deploymentStatus.setStatus(DeploymentStatusUtil.getStatus(v1PodList));
        deploymentStatus.setServiceName(context.getServiceName());

        try {
            ServiceAddress serviceAddress = getServiceAddress(context);
            if (serviceAddress != null) {
                deploymentStatus.setServiceAddress(serviceAddress.toString());
            }
        } catch (HyscaleException e) {
            logger.debug("Failed to get service address {} ", e.getHyscaleErrorCode());
        }

        return deploymentStatus;
    }

    public static void waitForPodActivity(ApiClient apiClient, V1PodHandler podHandler, String namespace,
                                          String selector, Predicate predicate, ActivityContext activityContext) throws HyscaleException {
        boolean podCondition = false;
        long startTime = System.currentTimeMillis();
        WorkflowLogger.startActivity(activityContext);
        while (!podCondition
                && (System.currentTimeMillis() - startTime < ResourceLifeCycleHandler.MAX_WAIT_TIME_IN_MILLISECONDS)) {
            WorkflowLogger.continueActivity(activityContext);
            try {
                List<V1Pod> podList = podHandler.getBySelector(apiClient, selector, true, namespace);
                if (podList != null && !podList.isEmpty()) {
                    podCondition = podList.stream().allMatch(predicate);
                }
            } catch (HyscaleException e) {
                logger.error("Error while waiting for Pod Activity for selector {} in namespace {}, error {}", selector,
                        namespace, e);
                WorkflowLogger.endActivity(activityContext, Status.FAILED);
                throw e;
            }
            ThreadPoolUtil.sleepSilently(MAX_POD_WAIT_SLEEP_INTERVAL_IN_MILLIS);
        }
        if (podCondition) {
            WorkflowLogger.endActivity(activityContext, Status.DONE);
        } else {
            WorkflowLogger.endActivity(activityContext, Status.FAILED);
            throw new HyscaleException(DeployerErrorCodes.FAILED_TO_INITIALIZE_POD);
        }
    }

    @Override
    public List<Pod> getPods(String namespace, String appName, String serviceName, K8sAuthorisation k8sAuthorisation)
            throws Exception {

        List<Pod> podList = new ArrayList<Pod>();
        List<V1Pod> v1PodList = null;
        try {
            ApiClient apiClient = clientProvider.get(k8sAuthorisation);
            V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
            V1PersistentVolumeClaimHandler v1PersistentVolumeClaimHandler = (V1PersistentVolumeClaimHandler) ResourceHandlers
                    .getHandlerOf(ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());

            String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);

            v1PodList = v1PodHandler.getBySelector(apiClient, selector, true, namespace);
            if (v1PodList == null || v1PodList.isEmpty()) {
                return null;
            }

            for (V1Pod v1Pod : v1PodList) {
                List<V1Volume> v1VolumeList = v1Pod.getSpec().getVolumes();
                List<Volume> podVolumeList = new ArrayList<Volume>();
                Set<String> volumeNames = new HashSet<>();
                if (v1VolumeList != null) {
                    for (V1Volume v1Volume : v1VolumeList) {
                        if (v1Volume.getPersistentVolumeClaim() != null) {
                            String claimName = v1Volume.getPersistentVolumeClaim().getClaimName();
                            if (claimName != null) {
                                Volume volume = new Volume();
                                volume.setName(v1Volume.getName());
                                volume.setClaimName(claimName);

                                V1PersistentVolumeClaim pvc = v1PersistentVolumeClaimHandler.get(apiClient, claimName,
                                        namespace);
                                if (pvc.getStatus() != null && pvc.getStatus().getCapacity() != null
                                        && pvc.getStatus().getCapacity().get(STORAGE) != null) {
                                    volume.setSize(pvc.getStatus().getCapacity().get(STORAGE).toSuffixedString());
                                }
                                volumeNames.add(volume.getName());
                                podVolumeList.add(volume);
                            }
                        }
                    }
                }
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

                Pod pod = new Pod();
                pod.setName(v1Pod.getMetadata().getName());
                logger.debug("Pod Name: " + v1Pod.getMetadata().getName());
                pod.setStatus(K8sPodUtil.getAggregatedStatusOfContainersForPod(v1Pod));
                logger.debug("Pod " + pod.getName() + ", status: " + pod.getStatus());
                List<Container> containers = new ArrayList<Container>();
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
                boolean ready = true;
                ready = ready && K8sPodUtil.checkForPodCondition(v1Pod, PodCondition.READY);
                pod.setReady(ready);
                pod.setContainers(containers);
                pod.setVolumes(podVolumeList);
                podList.add(pod);
            }
            return podList;
        } catch (HyscaleException e) {
            throw e;
        }
    }

    private VolumeMount getVolumeMount(V1VolumeMount v1VolumeMount) {
        VolumeMount volumeMount = new VolumeMount();
        volumeMount.setMountPath(v1VolumeMount.getMountPath());
        volumeMount.setName(v1VolumeMount.getName());
        if (v1VolumeMount.isReadOnly() != null) {
            volumeMount.setReadOnly(v1VolumeMount.isReadOnly());
        }
        return volumeMount;
    }

    private void deltaWait(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public List<ReplicaInfo> getReplicas(DeploymentContext context, boolean isFilter)
            throws HyscaleException {
        V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        String selector = ResourceSelectorUtil.getServiceSelector(context.getAppName(), context.getServiceName());
        ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());
        List<V1Pod> podList = v1PodHandler.getBySelector(apiClient, selector, true, context.getNamespace());
        
        if (podList == null || podList.isEmpty()) {
            return null;
        }
        
        if (!isFilter) {
            return K8sPodUtil.getReplicaInfo(podList);
        }
        
        // TODO filtering logic
        return null;
    }
}
