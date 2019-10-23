package io.hyscale.controller.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * This class executes 'hyscale get' command
 * It is a sub-command of the 'hyscale' command
 * @see HyscaleCtlCommand
 * Every command/sub-command has to implement the Runnable so that
 * whenever the command is executed the {@link #run()}
 * method will be invoked
 *
 * The sub-commands of are handled by @Command annotation
 *
 */
@Command(name = "get", subcommands = {HyscaleGetServiceCommand.class, HyscaleGetAppCommand.class},
        description = "Gets the specified resource.")
@Component
public class HyscaleGetCommand implements Runnable {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
    private boolean helpRequested = false;

    /**
     * Executes the 'hyscale get' command
     * Provides usage of this command to the user
     */
    @Override
    public void run() {
        new CommandLine(new HyscaleGetCommand()).usage(System.out);
    }

}
