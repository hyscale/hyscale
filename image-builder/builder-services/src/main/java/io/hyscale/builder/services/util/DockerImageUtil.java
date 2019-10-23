package io.hyscale.builder.services.util;

import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.commons.logger.WorkflowLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.services.command.ImageCommandGenerator;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ImageRegistry;

@Component
public class DockerImageUtil {

    private static final Logger logger = LoggerFactory.getLogger(DockerImageUtil.class);

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private ImageCommandGenerator commandGenerator;

    /*
     * To test if docker is installed and docker daemon is running
     */
    public void isDockerRunning() throws HyscaleException {
        String command = commandGenerator.getDockerInstalledCommand();
        logger.debug("Docker Installed check command: {}", command);
        boolean success = commandExecutor.execute(command);
        if (!success) {
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKER_NOT_INSTALLED);
        }
        command = commandGenerator.getDockerDaemonRunningCommand();
        logger.debug("Docker Daemon running check command: {}", command);
        success = commandExecutor.execute(command);
        if (!success) {
            WorkflowLogger.error(ImageBuilderActivity.DOCKER_DAEMON_NOT_RUNNING);
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKER_DAEMON_NOT_RUNNING);
        }
    }

    public void tagImage(String sourceImageFullPath, String targetImageFullPath) throws HyscaleException {

        String tagImageCommand = commandGenerator.getImageTagCommand(sourceImageFullPath, targetImageFullPath);

        logger.debug("Docker tag command: {}", tagImageCommand);
        boolean success = commandExecutor.execute(tagImageCommand);
        if (!success) {
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_TAG_IMAGE);
        }
    }

    public void pullImage(String imageName) throws HyscaleException {
        String pullImageCommand = commandGenerator.getImagePullCommand(imageName);

        logger.debug("Docker pull command: {}", pullImageCommand);
        boolean success = commandExecutor.execute(pullImageCommand);
        if (!success) {
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_PULL_IMAGE, imageName);
        }
    }

    public void loginToRegistry(ImageRegistry imageRegistry) throws HyscaleException {
        String loginCommand = commandGenerator.getLoginCommand(imageRegistry);
        boolean success = commandExecutor.execute(loginCommand);
        if (!success) {
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_LOGIN);
        }
    }

}
