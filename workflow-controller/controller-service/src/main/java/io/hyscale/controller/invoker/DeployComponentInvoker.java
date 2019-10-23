package io.hyscale.controller.invoker;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.LoggerUtility;
import io.hyscale.controller.plugins.K8SResourcesCleanUpHook;
import io.hyscale.controller.plugins.VolumeCleanUpHook;
import io.hyscale.controller.plugins.VolumeValidatorHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.component.ComponentInvoker;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.utils.LogProcessor;
import io.hyscale.controller.core.exception.ControllerErrorCodes;
import io.hyscale.deployer.services.config.DeployerConfig;
import io.hyscale.deployer.services.deployer.Deployer;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Deployer component acts as a bridge between workflow controller and deployer for deploy operation
 * provides link between {@link WorkflowContext} and {@link DeploymentContext}
 */
@Component
public class DeployComponentInvoker extends ComponentInvoker<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(DeployComponentInvoker.class);

    @Autowired
    private Deployer deployer;

    @Autowired
    private LoggerUtility loggerUtility;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    @Autowired
    private DeployerConfig deployerConfig;

    @Autowired
    private LogProcessor logProcessor;

    @Autowired
    private K8SResourcesCleanUpHook k8sResourcesCleanUpPlugin;

    @Autowired
    private VolumeCleanUpHook volumeCleanUpPlugin;

    @Autowired
    private VolumeValidatorHook volumeValidatorPlugin;

    @PostConstruct
    public void init() {
        super.addHook(volumeValidatorPlugin);
        super.addHook(k8sResourcesCleanUpPlugin);
        super.addHook(volumeCleanUpPlugin);
    }

    /**
     * Deploys the service to the kubernetes cluster
     * <p>
     * 1. Deploying the service to the cluster
     * 2. Wait for deployment completion
     * 3. Get service address from the cluster if externalized
     */
    @Override
    protected void doExecute(WorkflowContext context) throws HyscaleException {
        if (context == null || context.isFailed()) {
            return;
        }

        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            context.setFailed(true);
            logger.error("Service Spec is required for deployment");
            throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_REQUIRED);
        }

        List<Manifest> mainfestList = (List<Manifest>) context.getAttribute(WorkflowConstants.GENERATED_MANIFESTS);
        if (mainfestList == null || mainfestList.isEmpty()) {
            context.setFailed(true);
            logger.error("Manifest is required for deployment");
            throw new HyscaleException(ControllerErrorCodes.MANIFEST_REQUIRED);
        }
        Boolean verbose = (Boolean) context.getAttribute(WorkflowConstants.VERBOSE);

        verbose = (verbose != null) ? verbose : false;

        DeploymentContext deploymentContext = new DeploymentContext();
        deploymentContext.setAuthConfig(authConfigBuilder.getAuthConfig());
        deploymentContext.setNamespace(context.getNamespace());
        deploymentContext.setAppName(context.getAppName());
        deploymentContext.setManifests(mainfestList);

        String serviceName;
        try {
            serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
        } catch (HyscaleException e) {
            logger.error("Failed to get service name, error {}", e.toString());
            return;
        }

        deploymentContext.setServiceName(serviceName);
        context.setServiceName(serviceName);
        WorkflowLogger.header(ControllerActivity.STARTING_DEPLOYMENT);
        /*
            Deploys and waits for the deployment completion
         */

        try {
            deployer.deploy(deploymentContext);
            deployer.waitForDeployment(deploymentContext);

        } catch (HyscaleException e) {
            logger.error("Deployment failed with error: {}", e.toString());
            //WorkflowLogger.footer();
            //WorkflowLogger.error(ControllerActivity.DEPLOYMENT_FAILED,e.getMessage());
            throw e;
        } finally {
            writeDeployLogs(context, deploymentContext);
        }
        context.addAttribute(WorkflowConstants.OUTPUT, true);

        if (verbose) {
            loggerUtility.deploymentLogs(context);
        }

        Boolean external = serviceSpec.get(HyscaleSpecFields.external, Boolean.class);
        external = external == null ? false : external;
        logger.debug("Checking whether service {} is external {}", serviceName, external);
        if (external) {
            TypeReference<List<Port>> typeReference = new TypeReference<List<Port>>() {
            };
            List<Port> servicePorts = serviceSpec.get(HyscaleSpecFields.ports, typeReference);
            if (servicePorts != null) {
                ServiceAddress serviceAddress = deployer.getServiceAddress(deploymentContext);
                if (serviceAddress != null) {
                    context.addAttribute(WorkflowConstants.SERVICE_IP, serviceAddress.toString());
                }
            }
        }
    }

    /**
     * write deployment logs to file for later access
     *
     * @param context
     * @param deploymentContext
     */
    private void writeDeployLogs(WorkflowContext context, DeploymentContext deploymentContext) {
        try (InputStream is = deployer.logs(deploymentContext)) {
            String deploylogFile = deployerConfig.getDeployLogDir(deploymentContext.getAppName(),
                    deploymentContext.getServiceName());
            logProcessor.writeLogFile(is, deploylogFile);
            context.addAttribute(WorkflowConstants.DEPLOY_LOGS,
                    deployerConfig.getDeployLogDir(deploymentContext.getAppName(), deploymentContext.getServiceName()));
        } catch (IOException e) {
            logger.error("Failed to get deploy logs {}", deploymentContext.getServiceName(), e);
        } catch (HyscaleException ex) {
            logger.error("Failed to get deploy logs {}", deploymentContext.getServiceName(), ex);
        }
    }

    @Override
    protected void onError(WorkflowContext context, HyscaleException he) {
        WorkflowLogger.header(ControllerActivity.ERROR);
        WorkflowLogger.error(ControllerActivity.CAUSE, he != null ? he.getMessage() : DeployerErrorCodes.FAILED_TO_APPLY_MANIFEST.getErrorMessage());
        context.setFailed(true);
    }
}
