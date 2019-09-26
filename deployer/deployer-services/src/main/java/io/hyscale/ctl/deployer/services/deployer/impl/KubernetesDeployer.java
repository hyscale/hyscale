package io.hyscale.ctl.deployer.services.deployer.impl;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.ActivityContext;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.AuthConfig;
import io.hyscale.ctl.commons.models.DeploymentContext;
import io.hyscale.ctl.commons.models.K8sAuthorisation;
import io.hyscale.ctl.commons.models.Manifest;
import io.hyscale.ctl.commons.models.ResourceLabelKey;
import io.hyscale.ctl.commons.models.Status;
import io.hyscale.ctl.commons.models.YAMLManifest;
import io.hyscale.ctl.commons.utils.ResourceSelectorUtil;
import io.hyscale.ctl.commons.utils.ThreadPoolUtil;
import io.hyscale.ctl.deployer.core.model.DeploymentStatus;
import io.hyscale.ctl.deployer.core.model.ResourceKind;
import io.hyscale.ctl.deployer.services.config.DeployerConfig;
import io.hyscale.ctl.deployer.services.deployer.Deployer;
import io.hyscale.ctl.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.ctl.deployer.services.handler.ResourceHandlers;
import io.hyscale.ctl.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.ctl.deployer.services.handler.impl.V1PodHandler;
import io.hyscale.ctl.deployer.services.handler.impl.V1ServiceHandler;
import io.hyscale.ctl.deployer.services.model.DeployerActivity;
import io.hyscale.ctl.deployer.services.model.Pod;
import io.hyscale.ctl.deployer.services.model.ResourceStatus;
import io.hyscale.ctl.deployer.services.model.ServiceAddress;
import io.hyscale.ctl.deployer.services.predicates.PodPredicates;
import io.hyscale.ctl.deployer.services.provider.K8sClientProvider;
import io.hyscale.ctl.deployer.services.util.DeploymentStatusUtil;
import io.hyscale.ctl.deployer.services.util.K8sResourceDispatcher;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.util.Yaml;

@Component
public class KubernetesDeployer implements Deployer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesDeployer.class);
    private static final long DELTA_WAIT_MILLIS = 5000;
    private static final long MAX_POD_WAIT_SLEEP_INTERVAL_IN_MILLIS = 3000;

    @Autowired
    private DeployerConfig deployerConfig;

    @Autowired
    private K8sClientProvider clientProvider;

    @Override
    public void deploy(DeploymentContext context) throws HyscaleException {

        K8sResourceDispatcher resourceDispatcher = new K8sResourceDispatcher();
        try {
            resourceDispatcher.withClient(clientProvider.get((K8sAuthorisation) context.getAuthConfig()))
                    .waitForReadiness(context.isWaitForReadiness());
            resourceDispatcher.withNamespace(context.getNamespace()).withUpdatePolicy(deployerConfig.getUpdatePolicy())
                    .apply(context.getManifests());

        } catch (HyscaleException e) {
            LOGGER.error("Error while deploying service {} in namespace {} , error {} ", context.getServiceName(),
                    context.getNamespace(), e.toString());
            throw e;
        }
    }

    /*
     * Wait for Pod scheduled, creation and Readiness
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
        waitForPodActivity(apiClient, podHandler, namespace, selector,
                PodPredicates.isPodschedulingCondition(), podInitContext);

        // Pod Creation
        ActivityContext podCreationContext = new ActivityContext(DeployerActivity.POD_CREATION);
        waitForPodActivity(apiClient, podHandler, namespace, selector, PodPredicates.isPodCreated(), podCreationContext);

        // Pod readiness
        ActivityContext podReadinessContext = new ActivityContext(DeployerActivity.POD_READINESS);
        waitForPodActivity(apiClient, podHandler, namespace, selector, PodPredicates.isPodReady(), podReadinessContext);

    }

    @Override
    public ResourceStatus status(String namespace, Manifest manifest, AuthConfig authConfig) throws Exception {
        // get respective kind , name
        // get live manifest
        // query respective handler for status
        ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfig);

        YAMLManifest yamlManifest = (YAMLManifest) manifest;
        try {
            Object obj = Yaml.load(yamlManifest.getYamlManifest());
            Method kindMethod = obj.getClass().getMethod("getKind");
            Method metadataMethod = obj.getClass().getMethod("getMetadata");
            String kind = (String) kindMethod.invoke(obj);
            V1ObjectMeta v1ObjectMeta = (V1ObjectMeta) metadataMethod.invoke(obj);
            v1ObjectMeta.setNamespace(namespace);
            ResourceLifeCycleHandler lifeCycleHandler = ResourceHandlers.getHandlerOf(kind);
            String resourceName = metadataMethod.getName();
            return lifeCycleHandler.status(lifeCycleHandler.get(apiClient, resourceName, namespace));
        } catch (Exception e) {
            LOGGER.error("Error while preparing client, error {} ", e.toString());
            throw e;
        }
    }

    @Override
    public void unDeploy(DeploymentContext context) throws HyscaleException {
        K8sResourceDispatcher resourceDispatcher = new K8sResourceDispatcher();
        try {
            resourceDispatcher.withClient(clientProvider.get((K8sAuthorisation) context.getAuthConfig()));
            resourceDispatcher.withNamespace(context.getNamespace()).undeploy(context.getAppName(),
                    context.getServiceName());
        } catch (HyscaleException e) {
            LOGGER.error("Error while undeploying service in namespace {} , error {} ", context.getNamespace(),
                    e.toString());
            throw e;
        }
    }

    @Override
    public boolean authenticate(AuthConfig authConfig) {
        return false;
    }

    @Override
    public InputStream logs(DeploymentContext deploymentContext) throws HyscaleException {
        try {
            ApiClient apiClient = clientProvider.get((K8sAuthorisation) deploymentContext.getAuthConfig());
            V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
            String serviceName = deploymentContext.getServiceName();
            String namespace = deploymentContext.getNamespace();
            Integer readLines = deploymentContext.getReadLines();
            if (readLines == null) {
                readLines = deployerConfig.getDefaultTailLines();
            }
            if (deploymentContext.isTailLogs()) {
                return podHandler.tailLogs(apiClient, serviceName, namespace, readLines);
            } else {
                return podHandler.getLogs(apiClient, serviceName, namespace, readLines);
            }
        } catch (HyscaleException e) {
            LOGGER.error("Error while tailing logs, error {} ", e.toString());
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
            serviceAddress = v1ServiceHandler.getServiceAddress(apiClient, context.getServiceName(), context.getNamespace(),
                    context.isWaitForReadiness());
        } catch (HyscaleException e) {
            LOGGER.error("Error while preparing client, error {} ", e.toString());
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
            if (v1PodList == null || v1PodList.isEmpty()) {
                return DeploymentStatusUtil.getNotDeployedStatus(context.getServiceName());
            }
            deploymentStatus.setDateTime(DeploymentStatusUtil.getAge(v1PodList));
            deploymentStatus.setMessage(DeploymentStatusUtil.getMessage(v1PodList));
            deploymentStatus.setStatus(DeploymentStatusUtil.getStatus(v1PodList));
        } catch (HyscaleException e) {
            return DeploymentStatusUtil.getNotDeployedStatus(context.getServiceName());
        }
        try {
            ServiceAddress serviceAddress = getServiceAddress(context);
            if (serviceAddress != null) {
                deploymentStatus.setServiceAddress(serviceAddress.toString());
            }
        } catch (HyscaleException e) {
            LOGGER.debug("Failed to get serice address {} ", e.getHyscaleErrorCode());
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
            Set<String> serviceNames = v1PodList.stream().map(each -> {
                return each.getMetadata().getLabels().get(ResourceLabelKey.SERVICE_NAME.getLabel());
            }).collect(Collectors.toSet());

            if (serviceNames != null && !serviceNames.isEmpty()) {
                context.setWaitForReadiness(false);
                for (String each : serviceNames) {
                    context.setServiceName(each);
                    deploymentStatusList.add(getServiceDeploymentStatus(context));
                }
            }

        } catch (HyscaleException e) {
            LOGGER.error("Error while fetching status {} ", e);
            throw e;

        }
        return deploymentStatusList;
    }

    public static void waitForPodActivity(ApiClient apiClient, V1PodHandler podHandler,
                                          String namespace, String selector, Predicate predicate, ActivityContext activityContext) throws HyscaleException {
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
                LOGGER.error("Error while waiting for Pod Activity for selector {} in namespace {}, error {}",
                        selector, namespace, e);
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
    public List<Pod> getPods(String appName, String serviceName) {
        return null;
    }

    private void deltaWait(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
        }
    }
}
