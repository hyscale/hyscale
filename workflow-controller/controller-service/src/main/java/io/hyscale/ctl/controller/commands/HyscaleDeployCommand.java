package io.hyscale.ctl.controller.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Parent command for deploy operation
 *
 */
@Command(name = "deploy", subcommands = { HyscaleDeployServiceCommand.class}, description = "Deploys the specified resource")
@Component
public class HyscaleDeployCommand implements Runnable {

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
	private boolean helpRequested = false;

	@Override
	public void run() {
		new CommandLine(new HyscaleDeployCommand()).usage(System.out);
	}
}
