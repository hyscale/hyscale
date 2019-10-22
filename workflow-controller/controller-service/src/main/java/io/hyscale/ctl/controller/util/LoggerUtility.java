package io.hyscale.ctl.controller.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.builder.core.models.BuildContext;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.DeploymentContext;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.ctl.controller.constants.WorkflowConstants;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.ctl.deployer.services.util.DeployerLogUtil;
import io.hyscale.ctl.builder.services.util.ImageLogUtil;

/**
 * Utility to fetch deployment logs of the service specified.
 */
@Component
public class LoggerUtility {

	private static final Logger logger = LoggerFactory.getLogger(LoggerUtility.class);

	@Autowired
	private ImageLogUtil imageLogUtil;

	@Autowired
	private K8sAuthConfigBuilder authConfigBuilder;

	@Autowired
	private DeployerLogUtil deployerLogUtil;

	public void getLogs(WorkflowContext workflowContext) {
	    // Ignore image logs as they can be viewed at the directory
		// imageBuilderLogs(workflowContext);
		deploymentLogs(workflowContext);
	}

	/**
	 * deployment logs 
	 * @param context
	 */
	public void deploymentLogs(WorkflowContext context) {

		String serviceName = context.getServiceName();

		Boolean tail = (Boolean) context.getAttribute(WorkflowConstants.TAIL_LOGS);
		tail = (tail == null) ? false : tail;
		Integer lines = (Integer) context.getAttribute(WorkflowConstants.LINES);
		String appName = context.getAppName();
		DeploymentContext deploymentContext = new DeploymentContext();
		deploymentContext.setAuthConfig(authConfigBuilder.getAuthConfig());
		deploymentContext.setNamespace(context.getNamespace());
		deploymentContext.setAppName(appName);
		deploymentContext.setServiceName(serviceName);
		deploymentContext.setTailLogs(tail);
		deploymentContext.setReadLines(lines);

		try {
			WorkflowLogger.header(ControllerActivity.SERVICE_LOGS);
			deployerLogUtil.processLogs(deploymentContext);
		} catch (HyscaleException ex) {
			if (ex.getHyscaleErrorCode() == DeployerErrorCodes.FAILED_TO_RETRIEVE_POD) {
				WorkflowLogger.error(ControllerActivity.SERVICE_NOT_CREATED);
			} else {
				WorkflowLogger.error(ControllerActivity.FAILED_TO_STREAM_SERVICE_LOGS, ex.getMessage());
			}
			WorkflowLogger.error(ControllerActivity.CHECK_SERVICE_STATUS);
		} finally {
			WorkflowLogger.footer();
		}
		return;
	}

	/**
	 * Image build and push logs
	 * @param context
	 */
	public void imageBuilderLogs(WorkflowContext context) {
		BuildContext buildContext = new BuildContext();
		buildContext.setAppName(context.getAppName());
		buildContext.setServiceName(context.getServiceName());
		Boolean tail = (Boolean) context.getAttribute(WorkflowConstants.TAIL_LOGS);
		logger.debug("Getting Image Builder logs");
		tail = tail == null ? false : tail;
		buildContext.setTail(tail);
		imageLogUtil.handleLogs(buildContext);

	}
}
