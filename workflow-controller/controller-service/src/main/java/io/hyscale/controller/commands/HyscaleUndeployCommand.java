package io.hyscale.controller.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *  This class executes 'hyscale undeploy' command
 *  It is a sub-command of the 'hyscale' command
 *  @see HyscaleCtlCommand
 *  Every command/sub-command has to implement the Runnable so that
 *  whenever the command is executed the {@link #run()}
 *  method will be invoked
 *
 * The sub-commands of are handled by @Command annotation
 *
 */
@Command(name = "undeploy", subcommands = {HyscaleUndeployAppCommand.class,
		HyscaleUndeploySeviceCommand.class }, description = "Undeploys the specified resource")
@Component
public class HyscaleUndeployCommand implements Runnable {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Display help message")
	private boolean helpRequested = false;

	/**
	 * Executes the 'hyscale undeploy' command
	 * Provides usage of this command to the user
	 */
	@Override
	public void run() {
		new CommandLine(new HyscaleUndeployCommand()).usage(System.out);
	}

}
