package io.hyscale.controller.commands;

import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.UndeployCommandUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.invoker.UndeployComponentInvoker;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *  This class executes 'hyscale undeploy service' command
 *  It is a sub-command of the 'hyscale undeploy' command
 *  @see HyscaleUndeployCommand
 *  Every command/sub-command has to implement the Runnable so that
 *  whenever the command is executed the {@link #run()}
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
 * Removes all the resources from the cluster related to the
 * given service from the namespace.
 *
 */
@Command(name = "service", description = "Undeploy service from the configured kubernetes cluster")
@Component
public class HyscaleUndeploySeviceCommand implements Runnable {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Displays the help information of the specified command")
	private boolean helpRequested = false;

	@Option(names = { "-n", "--namespace", "-ns" }, required = true, description = "Namespace of the deployed service")
	private String namespace;

	@Option(names = { "-a", "--app" }, required = true, description = "Application name")
	private String appName;

	@Option(names = { "-s", "--service" }, required = true, description = "Service name")
	private String[] serviceList;

	@Autowired
	private UndeployComponentInvoker undeployComponentInvoker;

	@Override
	public void run() {
		WorkflowContext workflowContext = new WorkflowContext();
		workflowContext.addAttribute(WorkflowConstants.CLEAN_UP_SERVICE_DIR, true);
		workflowContext.setAppName(appName.trim());
		workflowContext.setNamespace(namespace.trim());

		for (int i = 0; i < serviceList.length; i++) {
			String serviceName = serviceList[i];
			WorkflowLogger.header(ControllerActivity.SERVICE_NAME, serviceName);
			workflowContext.setServiceName(serviceName);
			undeployComponentInvoker.execute(workflowContext);
		}
		UndeployCommandUtil.logUndeployInfo();
	}

}
