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
 *  This class executes 'hyscale undeploy app' command
 *  It is a sub-command of the 'hyscale undeploy' command
 *  @see HyscaleUndeployCommand
 *  Every command/sub-command has to implement the Runnable so that
 *  whenever the command is executed the {@link #run()}
 *  method will be invoked
 *
 * @option namespace  name of the namespace from which the
 * 					  app has to be undeployed
 * @option appName    name of the app to be undeployed
 *
 * Eg: hyscale undeploy app -a sample -n dev
 *
 * Undeploys the app from the given namespace in the
 * configured kubernetes cluster.
 * Undeploys all the resources from the cluster related to the
 * app from the namespace.
 * @Note Note: Undeploy does not clear the namespace
 *
 */
@Command(name = "app", description = "Undeploys app from the kubernetes cluster")
@Component
public class HyscaleUndeployAppCommand implements Runnable {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Display the help information of the specified command")
	private boolean helpRequested = false;

	@Option(names = { "-n", "--namespace", "-ns" }, required = true, description = "Namespace of the deployed app")
	private String namespace;

	@Option(names = { "-a", "--app" }, required = true, description = "Application name")
	private String appName;

	@Autowired
	private UndeployComponentInvoker undeployComponentInvoker;

	@Override
	public void run() {

		WorkflowContext workflowContext = new WorkflowContext();
		workflowContext.setAppName(appName.trim());
		workflowContext.setNamespace(namespace.trim());
		workflowContext.addAttribute(WorkflowConstants.CLEAN_UP_APP_DIR, true);
		WorkflowLogger.header(ControllerActivity.APP_NAME, appName);
		undeployComponentInvoker.execute(workflowContext);
		
		UndeployCommandUtil.logUndeployInfo();
	}

}
