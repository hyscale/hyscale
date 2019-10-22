package io.hyscale.ctl.controller.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

/**
 * This class executes 'hyscale generate service' command
 * It is a sub-command of the 'hyscale generate' command
 * @see HyscaleGenerateCommand
 * Every command/sub-command has to implement the Runnable so that
 * whenever the command is executed the {@link #run()}
 * method will be invoked
 *
 * The sub-commands of are handled by @Command annotation
 *
 */
@CommandLine.Command(name = "service", subcommands = { HyscaleGenerateServiceManifestsCommand.class} ,
        description = "Performs action on the service")
@Component
public class HyscaleGenerateServiceCommand implements Runnable {
    @Override
    public void run() {
        new CommandLine(new HyscaleGenerateServiceCommand()).usage(System.out);
    }
}
