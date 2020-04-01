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
package io.hyscale.controller.commands;

import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.executors.UndeployComponentExecutor;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.controller.util.UndeployCommandUtil;

import java.util.List;
import java.util.concurrent.Callable;

import javax.validation.constraints.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *  This class executes 'hyscale undeploy service' command
 *  It is a sub-command of the 'hyscale undeploy' command
 *  @see HyscaleUndeployCommand
 *  Every command/sub-command has to implement the {@link Callable} so that
 *  whenever the command is executed the {@link #call()}
 *  method will be invoked
 *
 * @option namespace  name of the namespace from which the
 * 					  service has to be undeployed
 * @option appName    name of the app in which the service is present
 * @option serviceList  list of service names to be undeployed
 *
 * Eg: hyscale undeploy service -s s1 -s s2 -a sample -n dev
 *
 * Undeploys the service from the namespace in the cluster
 * Removes all the resources, except the pvcs from the cluster related 
 * to the given service from the namespace.
 *
 */
@Command(name = "service", description = "Undeploy service from the configured kubernetes cluster")
@Component
public class HyscaleUndeploySeviceCommand implements Callable<Integer> {
    
    private static final Logger logger = LoggerFactory.getLogger(HyscaleUndeploySeviceCommand.class);

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Displays the help information of the specified command")
	private boolean helpRequested = false;

	@Pattern(regexp = ValidationConstants.NAMESPACE_REGEX, message = ValidationConstants.INVALID_NAMESPACE_MSG)
	@Option(names = { "-n", "--namespace", "-ns" }, required = true, description = "Namespace of the deployed service")
	private String namespace;

	@Pattern(regexp = ValidationConstants.APP_NAME_REGEX, message = ValidationConstants.INVALID_APP_NAME_MSG)
	@Option(names = { "-a", "--app" }, required = true, description = "Application name")
	private String appName;

	@Option(names = { "-s", "--service" }, required = true, description = "Service names", split = ",")
	private List<
	@Pattern(regexp = ValidationConstants.SERVICE_NAME_REGEX, message = ValidationConstants.INVALID_SERVICE_NAME_MSG)
	String> serviceList;

	@Autowired
	private UndeployComponentExecutor undeployComponentInvoker;

	@Override
	public Integer call() throws Exception {
	    if (!CommandUtil.isInputValid(this)) {
	        return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }
	    boolean isFailed = false;
		WorkflowContext workflowContext = new WorkflowContext();
		workflowContext.addAttribute(WorkflowConstants.CLEAN_UP_SERVICE_DIR, true);
		workflowContext.setAppName(appName.trim());
		workflowContext.setNamespace(namespace.trim());

		for (String serviceName: serviceList) {
		    WorkflowLogger.header(ControllerActivity.SERVICE_NAME, serviceName);
		    workflowContext.setServiceName(serviceName);
		    try {
		        undeployComponentInvoker.execute(workflowContext);
		    } catch (HyscaleException e) {
		        logger.error("Error while undeploying app: {}, service: {}, in namespace: {}", appName, serviceName, namespace, e);
		        isFailed = true;
            } finally {
                UndeployCommandUtil.logUndeployInfo();
            }
		}
		return isFailed ? ToolConstants.HYSCALE_ERROR_CODE : 0;
	}

}
