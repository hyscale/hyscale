package io.hyscale.ctl.controller.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

/**
 * Parent command for get service operation
 *
 */
@CommandLine.Command(name = "service", subcommands = { HyscaleServiceLogsCommand.class,
		HyscaleServiceStatusCommand.class }, description = "Performs action on the service")
@Component
public class HyscaleGetServiceCommand implements Runnable {

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
	private boolean helpRequested = false;

	@Override
	public void run() {
		new CommandLine(new HyscaleGetServiceCommand()).usage(System.out);
	}
}
