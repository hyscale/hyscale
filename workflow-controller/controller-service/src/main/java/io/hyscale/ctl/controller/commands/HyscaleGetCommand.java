package io.hyscale.ctl.controller.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "get", subcommands = { HyscaleGetServiceCommand.class, HyscaleGetAppCommand.class },
		description = "Gets the specified resource.")
@Component
public class HyscaleGetCommand implements Runnable {

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
	private boolean helpRequested = false;

	@Override
	public void run() {
		new CommandLine(new HyscaleGetCommand()).usage(System.out);
	}

}
