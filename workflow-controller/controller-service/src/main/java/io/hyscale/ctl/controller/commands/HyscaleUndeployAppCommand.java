package io.hyscale.ctl.controller.commands;

import io.hyscale.ctl.controller.constants.WorkflowConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.controller.invoker.UndeployComponentInvoker;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

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
		WorkflowLogger.info(ControllerActivity.UNDEPLOYMENT_DONE);
	}

}
