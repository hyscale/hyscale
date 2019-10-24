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
package io.hyscale.controller.util;

import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.model.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.util.DeployerLogUtil;
import io.hyscale.builder.services.util.ImageLogUtil;

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
