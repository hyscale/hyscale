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
		WorkflowLogger.info(ControllerActivity.UNDEPLOYMENT_DONE);
	}

}
