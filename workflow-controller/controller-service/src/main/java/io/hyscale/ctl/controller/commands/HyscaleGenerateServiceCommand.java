package io.hyscale.ctl.controller.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine;

/**
 * Parent command for generate service operation
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
