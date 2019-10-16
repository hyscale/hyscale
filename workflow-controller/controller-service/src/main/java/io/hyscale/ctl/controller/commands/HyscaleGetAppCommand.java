package io.hyscale.ctl.controller.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

/**
 * Parent COmmand for get app operation
 *
 */
@CommandLine.Command(name = "app", subcommands = { HyscaleAppStatusCommand.class }, description = "Operates on the application specified.")
@Component
public class HyscaleGetAppCommand implements Runnable {

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
	private boolean helpRequested = false;

	@Override
	public void run() {
		new CommandLine(new HyscaleGetAppCommand()).usage(System.out);
	}
}
