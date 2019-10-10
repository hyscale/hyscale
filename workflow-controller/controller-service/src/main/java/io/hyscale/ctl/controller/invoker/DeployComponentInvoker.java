package io.hyscale.ctl.controller.invoker;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.ctl.commons.component.ComponentInvoker;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.DeploymentContext;
import io.hyscale.ctl.commons.models.Manifest;
import io.hyscale.ctl.commons.utils.LogProcessor;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.ctl.controller.constants.WorkflowConstants;
import io.hyscale.ctl.controller.core.exception.ControllerErrorCodes;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.controller.plugins.K8sResourcesCleanUpPlugin;
import io.hyscale.ctl.controller.plugins.VolumeCleanUpPlugin;
import io.hyscale.ctl.controller.plugins.VolumeValidatorPlugin;
import io.hyscale.ctl.controller.util.LoggerUtility;
import io.hyscale.ctl.deployer.services.config.DeployerConfig;
import io.hyscale.ctl.deployer.services.deployer.Deployer;
import io.hyscale.ctl.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.ctl.deployer.services.model.ServiceAddress;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.Port;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

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
	private K8sResourcesCleanUpPlugin k8sResourcesCleanUpPlugin;
	
	@Autowired
	private VolumeCleanUpPlugin volumeCleanUpPlugin;
	
	@Autowired
	private VolumeValidatorPlugin volumeValidatorPlugin;

    @PostConstruct
    public void init() {
    	super.addPlugin(volumeValidatorPlugin);
    	super.addPlugin(k8sResourcesCleanUpPlugin);
		super.addPlugin(volumeCleanUpPlugin);
    }

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
            WorkflowLogger.logPersistedActivities();
        }
        context.addAttribute(WorkflowConstants.OUTPUT, true);

        if (verbose) {
            loggerUtility.deploymentLogs(context);
        }

        Boolean external = serviceSpec.get(HyscaleSpecFields.external, Boolean.class);
        external = external == null ? false : external;
        logger.debug("Checking whether service {} is external {}", serviceName ,external);
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
