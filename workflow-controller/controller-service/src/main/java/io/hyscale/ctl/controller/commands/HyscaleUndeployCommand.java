package io.hyscale.ctl.controller.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Parent command for undeploy operation
 *
 */
@Command(name = "undeploy", subcommands = {HyscaleUndeployAppCommand.class,
		HyscaleUndeploySeviceCommand.class }, description = "Undeploys the specified resource")
@Component
public class HyscaleUndeployCommand implements Runnable {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Display help message")
	private boolean helpRequested = false;

	@Override
	public void run() {
		new CommandLine(new HyscaleUndeployCommand()).usage(System.out);
	}

}
