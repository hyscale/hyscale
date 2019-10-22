package io.hyscale.ctl.controller.invoker;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.component.ComponentInvoker;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.DeploymentContext;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.ctl.controller.core.exception.ControllerErrorCodes;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.controller.plugins.AppDirCleanUpPlugin;
import io.hyscale.ctl.controller.plugins.ServiceDirCleanUpPlugin;
import io.hyscale.ctl.deployer.services.deployer.Deployer;

/**
 *	Undeploy component acts as a bridge between workflow controller and deployer for undeploy operation
 *	provides link between {@link WorkflowContext} and {@link DeploymentContext}
 */
@Component
public class UndeployComponentInvoker extends ComponentInvoker<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(UndeployComponentInvoker.class);

    @Autowired
    private Deployer deployer;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    @Autowired
    private ServiceDirCleanUpPlugin serviceDirCleanUpPlugin;

    @Autowired
    private AppDirCleanUpPlugin appDirCleanUpPlugin;

    @PostConstruct
    public void init() {
        addPlugin(serviceDirCleanUpPlugin);
        addPlugin(appDirCleanUpPlugin);
    }
    
    
    @Override
    protected void doExecute(WorkflowContext context) throws HyscaleException {
        if (context == null) {
            return;
        }
        WorkflowLogger.header(ControllerActivity.STARTING_UNDEPLOYMENT);
        DeploymentContext deploymentContext = new DeploymentContext();
        deploymentContext.setAuthConfig(authConfigBuilder.getAuthConfig());
        deploymentContext.setNamespace(context.getNamespace());
        deploymentContext.setAppName(context.getAppName());
        deploymentContext.setServiceName(context.getServiceName());

        try {
            deployer.unDeploy(deploymentContext);
        } catch (HyscaleException ex) {
            WorkflowLogger.error(ControllerActivity.UNDEPLOYMENT_FAILED, ex.getMessage());
            throw ex;
        } finally {
            WorkflowLogger.footer();
        }
    }

    @Override
    protected void onError(WorkflowContext context, HyscaleException he) {
        WorkflowLogger.header(ControllerActivity.ERROR);
        WorkflowLogger.error(ControllerActivity.CAUSE, he != null ?
                he.getMessage() : ControllerErrorCodes.UNDEPLOYMENT_FAILED.getErrorMessage());
        context.setFailed(true);
    }

}
