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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.commands.provider.DeployCommandProvider;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.ActivityContext;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.models.K8sBasicAuth;
import io.hyscale.commons.models.K8sConfigFileAuth;
import io.hyscale.commons.models.KubernetesResource;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.models.YAMLManifest;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.commons.utils.ThreadPoolUtil;
import io.hyscale.deployer.core.model.AppMetadata;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.core.model.ReplicaInfo;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.builder.AppMetadataBuilder;
import io.hyscale.deployer.services.config.DeployerConfig;
import io.hyscale.deployer.services.deployer.Deployer;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.handler.impl.V1PersistentVolumeClaimHandler;
import io.hyscale.deployer.services.handler.impl.V1PodHandler;
import io.hyscale.deployer.services.handler.impl.V1ServiceHandler;
import io.hyscale.deployer.services.model.Container;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.model.Pod;
import io.hyscale.deployer.services.model.PodCondition;
import io.hyscale.deployer.services.model.ResourceStatus;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.hyscale.deployer.services.model.Volume;
import io.hyscale.deployer.services.model.VolumeMount;
import io.hyscale.deployer.services.predicates.PodPredicates;
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
import io.kubernetes.client.util.KubeConfig;

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
    
    @Autowired
    private AppMetadataBuilder appMetadataBuilder;
    
    @Autowired
    private DeployCommandProvider deployCommandProvider;
    
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
	public int exec(K8sAuthorisation k8sAuthorisation, String servicename, String appname, String namespace,
			String replicaName) throws HyscaleException {

		if (k8sAuthorisation == null) {
			throw new HyscaleException(DeployerErrorCodes.K8SAUTHORISATION_NOT_FOUND);
		}
		List<String> commands = null;
		KubeConfig kubeConfig = null;
		int exitCode = 0;

		switch (k8sAuthorisation.getK8sAuthType()) {

		case BASIC_AUTH:
			K8sBasicAuth k8sBasicAuth = (K8sBasicAuth) k8sAuthorisation;
			commands = deployCommandProvider.getExecCommandByBasicAuth(replicaName, namespace, k8sBasicAuth);
			break;

		case KUBE_CONFIG_FILE:
			K8sConfigFileAuth k8sConfigFileAuth = (K8sConfigFileAuth) k8sAuthorisation;
			kubeConfig = getKubeConfig(k8sConfigFileAuth);
			if (kubeConfig.getAccessToken() != null) {
				commands = deployCommandProvider.getExecCommandByAccessToken(replicaName, namespace,
						kubeConfig.getAccessToken());
				exitCode = CommandExecutor.executeWithParentIO(commands);
			}
			break;

		default:
			break;
		}
		if (exitCode != 0) {
			throw new HyscaleException(DeployerErrorCodes.FAILED_TO_EXEC_INTO_POD, replicaName,
					Integer.toString(exitCode));
		}
		return exitCode;
	}

	private KubeConfig getKubeConfig(K8sConfigFileAuth k8sConfigFileAuth) {
		InputStreamReader isr = null;
		try {
			isr = new FileReader(k8sConfigFileAuth.getK8sConfigFile());
		} catch (FileNotFoundException e) {
			logger.error("Error while reading kubeconfig file {}", e.toString());
		}
		return KubeConfig.loadKubeConfig(isr);
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

        String serviceName = context.getServiceName();
        if (context != null && StringUtils.isBlank(serviceName)) {
            throw new HyscaleException(DeployerErrorCodes.SERVICE_REQUIRED);
        }

        List<V1Pod> v1PodList = null;
        try {
            ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());
            V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
            String selector = ResourceSelectorUtil.getServiceSelector(context.getAppName(), serviceName);
            v1PodList = v1PodHandler.getBySelector(apiClient, selector, true, context.getNamespace());
            if (v1PodList == null || v1PodList.isEmpty()) {
                List<DeploymentStatus> serviceStatus = K8sDeployerUtil.getOwnerDeploymentStatus(apiClient, context);
                return serviceStatus != null ? serviceStatus.get(0) : null;
            }
        } catch (HyscaleException e) {
            logger.error("Error while fetching status {} ", e);
            throw e;
        }
        
        return getPodDeploymentStatus(context, v1PodList);
    }

    /**
     * Get Status for services based on app label
     */
    @Override
    public List<DeploymentStatus> getDeploymentStatus(DeploymentContext context) throws HyscaleException {
        List<DeploymentStatus> deploymentStatusList = new ArrayList<>();
        try {
            ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());
            
            Set<String> services = K8sDeployerUtil.getDeployedServices(apiClient, context);
            for (String serviceName: services) {
                context.setServiceName(serviceName);
                deploymentStatusList.add(getServiceDeploymentStatus(context));
            }
        } catch (HyscaleException e) {
            logger.error("Error while fetching status {} ", e);
            throw e;

        }
        return deploymentStatusList;
    }
    
    private DeploymentStatus getPodDeploymentStatus(DeploymentContext context, List<V1Pod> v1PodList) {

        if (v1PodList == null || v1PodList.isEmpty()) {
            return null;
        }
        DeploymentStatus deploymentStatus = new DeploymentStatus();
        deploymentStatus.setServiceName(context.getServiceName());
        deploymentStatus.setAge(DeploymentStatusUtil.getAge(v1PodList));
        deploymentStatus.setMessage(DeploymentStatusUtil.getMessage(v1PodList));
        deploymentStatus.setStatus(DeploymentStatusUtil.getStatus(v1PodList));

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
                        Volume volume = getVolume(apiClient, v1Volume, namespace);
                        if (volume != null) {
                            volumeNames.add(volume.getName());
                            podVolumeList.add(volume);
                        }
                    }
                }
                Pod pod = new Pod();
                pod.setName(v1Pod.getMetadata().getName());
                logger.debug("Pod Name: " + pod.getName());
                pod.setStatus(K8sPodUtil.getAggregatedStatusOfContainersForPod(v1Pod));
                logger.debug("Pod: " + pod.getName() + ", status: " + pod.getStatus());
                List<Container> containers = getContainers(v1Pod, volumeNames);
                
                boolean ready = K8sPodUtil.checkForPodCondition(v1Pod, PodCondition.READY);
                pod.setReady(ready);
                pod.setContainers(containers);
                pod.setVolumes(podVolumeList);
                podList.add(pod);
            }
            return podList;
        } catch (HyscaleException e) {
            logger.error("Error while getting Pods, error {}", e.getMessage());
            throw e;
        }
    }
    
    private List<Container> getContainers(V1Pod v1Pod, Set<String> volumeNames){
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

    private void deltaWait(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
        }
    }
    
    /**
     * Get Replica info for pods
     * Implementation:
     * <p>
     * isFilter if disabled return replica info for all pods fetched based on selector
     * if enabled,
     * 1. If owner kind for pods is different, warn user and return replica info for all pods
     * 2. If owner kind is {@link ResourceKind #DEPLOYMENT} or {@link ResourceKind #REPLICA_SET},
     *     Get Revision from deployment, get replicas set with this revision,
     *     filter pods based on pod-template-hash from replica set
     *     return replica info for filtered pods
     * 3. Else return replica info for all pods
     * </p>
     */
    @Override
    public List<ReplicaInfo> getReplicas(AuthConfig authConfig, String appName, String serviceName, String namespace, 
            boolean isFilter) throws HyscaleException {
        V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
        ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfig);
        List<V1Pod> podList = v1PodHandler.getBySelector(apiClient, selector, true, namespace);
        
        if (podList == null || podList.isEmpty()) {
            return null;
        }
        
        if (!isFilter) {
            return K8sReplicaUtil.getReplicaInfo(podList);
        }
        
        if (!PodPredicates.isPodAmbiguous().test(podList)) {
            return K8sReplicaUtil.getReplicaInfo(podList);
        }
        String podOwner = K8sPodUtil.getPodsUniqueOwner(podList);
        ResourceKind podOwnerKind = ResourceKind.fromString(podOwner);
        
        // Unknown parent
        if (StringUtils.isBlank(podOwner)) {
            logger.debug("Unable to determine latest deployment, displaying all replicas");
            WorkflowLogger.warn(DeployerActivity.LATEST_DEPLOYMENT_NOT_IDENTIFIABLE);
            return K8sReplicaUtil.getReplicaInfo(podList);
        }
        
        // Deployment
        if (ResourceKind.REPLICA_SET.equals(podOwnerKind) || ResourceKind.DEPLOYMENT.equals(podOwnerKind)) {
            // Get deployment, get revision, get RS with the revision, get all labels and filter pods
            return K8sReplicaUtil.getReplicaInfo(K8sDeployerUtil.filterPodsByDeployment(apiClient, appName, serviceName, namespace, podList));
        }
        
        // TODO do we need to handle STS cases ??
        logger.debug("Replicas info:: unhandled case, pod owner: {}", podOwner);
        return K8sReplicaUtil.getReplicaInfo(podList);
    }

    @Override
    public List<AppMetadata> getAppsMetadata(AuthConfig authConfig) throws HyscaleException {
        ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfig);

        V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        
        List<V1Pod> podList  = podHandler.getPodsForAllNamespaces(apiClient);
        
        return appMetadataBuilder.build(podList);
    }
    
}
