package io.hyscale.ctl.controller.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Aggregator parent command
 */
@Command(name = "hyscale", versionProvider = HyscaleVersionProvider.class, mixinStandardHelpOptions = true, subcommands = {
		HyscaleGetCommand.class, HyscaleDeployCommand.class, HyscaleUndeployCommand.class,
		HyscaleGenerateCommand.class})
@Component
public class HyscaleCtlCommand implements Runnable {

	@Override
	public void run() {
		new CommandLine(new HyscaleCtlCommand()).usage(System.out);
	}

}
