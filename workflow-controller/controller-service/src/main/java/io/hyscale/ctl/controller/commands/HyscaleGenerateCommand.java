package io.hyscale.ctl.controller.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;

@Command(name = "generate", subcommands = { HyscaleGenerateServiceCommand.class }, description = { "Generates the specified resource" })
@Component
public class HyscaleGenerateCommand implements Runnable {

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
	private boolean helpRequested = false;

	@Override
	public void run() {
		new CommandLine(new HyscaleGenerateCommand()).usage(System.out);
	}
}
