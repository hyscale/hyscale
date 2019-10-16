package io.hyscale.ctl.controller.initializer;

import io.hyscale.ctl.builder.services.config.ImageBuilderConfig;
import io.hyscale.ctl.builder.core.models.ImageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import io.hyscale.ctl.commons.config.SetupConfig;
import io.hyscale.ctl.commons.constants.ToolConstants;
import io.hyscale.ctl.commons.utils.ThreadPoolUtil;
import io.hyscale.ctl.controller.commands.HyscaleCtlCommand;
import io.hyscale.ctl.controller.core.exception.ControllerErrorCodes;
import io.hyscale.ctl.controller.util.ShutdownHook;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.ParameterException;

/**
 * Entry point for executing commands
 *
 */
@SpringBootApplication
@ComponentScan("io.hyscale.ctl")
public class HyscaleCtlInitializer implements CommandLineRunner {

    private final Logger LOGGER = LoggerFactory.getLogger(HyscaleCtlInitializer.class);

    @Autowired
    private IFactory factory;

    static {
        System.setProperty(ImageBuilderConfig.IMAGE_BUILDER_PROP, ImageBuilder.LOCAL.name());
        System.setProperty(ToolConstants.HYSCALECTL_LOGS_DIR_PROPERTY, SetupConfig.getToolLogDir());
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(HyscaleCtlInitializer.class);
        app.run(args);
    }

    public void run(String... args) {

        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
            CommandLine commandLine = new CommandLine(new HyscaleCtlCommand(), factory);
            commandLine.execute(args);
        } catch (ParameterException e) {
            LOGGER.error("Error while processing command, error {}", ControllerErrorCodes.INVALID_COMMAND.getErrorMessage(), e);
        } finally {
            ThreadPoolUtil.getInstance().shutdown();
        }
    }

}
