/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

@Component
public class DockerImageUtil {

    private static final Logger logger = LoggerFactory.getLogger(DockerImageUtil.class);

    @Autowired
    private ImageCommandGenerator commandGenerator;

    /**
     * Test if docker is installed and docker daemon is running
     */
    public void isDockerRunning() throws HyscaleException {
        String command = commandGenerator.getDockerInstalledCommand();
        logger.debug("Docker Installed check command: {}", command);
        boolean success = CommandExecutor.execute(command);
        if (!success) {
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKER_NOT_INSTALLED);
        }
        command = commandGenerator.getDockerDaemonRunningCommand();
        logger.debug("Docker Daemon running check command: {}", command);
        success = CommandExecutor.execute(command);
        if (!success) {
            WorkflowLogger.error(ImageBuilderActivity.DOCKER_DAEMON_NOT_RUNNING);
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKER_DAEMON_NOT_RUNNING);
        }
    }

    public void tagImage(String sourceImageFullPath, String targetImageFullPath) throws HyscaleException {

        String tagImageCommand = commandGenerator.getImageTagCommand(sourceImageFullPath, targetImageFullPath);

        logger.debug("Docker tag command: {}", tagImageCommand);
        boolean success = CommandExecutor.execute(tagImageCommand);
        if (!success) {
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_TAG_IMAGE);
        }
    }

    public void pullImage(String imageName) throws HyscaleException {
        String pullImageCommand = commandGenerator.getImagePullCommand(imageName);

        logger.debug("Docker pull command: {}", pullImageCommand);
        boolean success = CommandExecutor.execute(pullImageCommand);
        if (!success) {
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_PULL_IMAGE, imageName);
        }
    }

}
