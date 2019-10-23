package io.hyscale.controller.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * This class is the first level command for hyscale tool.
 * This class executes 'hyscale' command
 *
 * Every command/sub-command has to implement the Runnable so that
 * whenever the command is executed the {@link #run()}
 * method will be invoked
 * <p>
 *
 * Command annotation overrides the version provider that picoli provides
 * by default, with {@link HyscaleVersionProvider} as implementation
 *
 * Also check the sub-commands at @Command annotation
 * </p>
 */
@Command(name = "hyscale", versionProvider = HyscaleVersionProvider.class, mixinStandardHelpOptions = true, subcommands = {
        HyscaleGetCommand.class, HyscaleDeployCommand.class, HyscaleUndeployCommand.class,
        HyscaleGenerateCommand.class})
@Component
public class HyscaleCtlCommand implements Runnable {

    /**
     * Executes the hyscale command
     * Provides usage of this command to the user.
     */
    @Override
    public void run() {
        new CommandLine(new HyscaleCtlCommand()).usage(System.out);
    }

}
