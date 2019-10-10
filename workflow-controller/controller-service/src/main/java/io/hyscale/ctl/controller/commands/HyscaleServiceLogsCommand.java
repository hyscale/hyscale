package io.hyscale.ctl.controller.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.constants.WorkflowConstants;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.controller.util.LoggerUtility;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "logs", aliases = { "log" }, description = "Displays the service logs")
@Component
public class HyscaleServiceLogsCommand implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(HyscaleServiceLogsCommand.class);

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Displays help information for the specified command")
	private boolean helpRequested = false;

	@Option(names = { "-n", "--namespace", "-ns" }, required = true, description = "Namespace of the service")
	private String namespace;

	@Option(names = { "-a", "--app" }, required = true, description = "Application name")
	private String appName;

	@Option(names = { "-s", "--service" }, required = true, description = "Service name")
	private String serviceName;

	@Option(names = { "-t", "--tail" }, required = false, description = "Tail output of the service logs")
	private boolean tail = false;

	@Option(names = { "-l", "--line" }, required = false, description = "Number of lines of logs")
	private Integer line = 100;

	@Autowired
	private LoggerUtility loggerUtility;

	@Override
	public void run() {

		WorkflowContext workflowContext = new WorkflowContext();
		WorkflowLogger.header(ControllerActivity.SERVICE_NAME, serviceName);
		workflowContext.setAppName(appName.trim());
		workflowContext.setNamespace(namespace.trim());
		workflowContext.setServiceName(serviceName);
		workflowContext.addAttribute(WorkflowConstants.TAIL_LOGS, tail);
		workflowContext.addAttribute(WorkflowConstants.LINES, line);

		loggerUtility.getLogs(workflowContext);

	}
}
