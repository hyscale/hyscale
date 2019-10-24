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
package io.hyscale.builder.services.impl;

import java.io.File;

import io.hyscale.builder.services.command.ImageCommandGenerator;
import io.hyscale.builder.services.config.LocalImageBuildCondition;
import io.hyscale.builder.services.util.DockerImageUtil;
import io.hyscale.builder.services.util.ImageLogUtil;
import io.hyscale.builder.services.service.ImageBuildService;
import io.hyscale.commons.config.SetupConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.core.models.DockerImage;
import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.commons.models.Status;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
@Conditional(LocalImageBuildCondition.class)
public class LocalImageBuildServiceImpl implements ImageBuildService {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageBuildServiceImpl.class);

    @Autowired
    private CommandExecutor commandExecutor;

    @Autowired
    private ImageCommandGenerator imageCommandGenerator;

    @Autowired
    private DockerImageUtil dockerImageUtil;

    @Autowired
    private ImageLogUtil imageLogUtil;

    @Autowired
    private ImageBuilderConfig imageBuilderConfig;

    @Override
    public BuildContext build(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {

        validate(serviceSpec, context);

        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_BUILD_STARTED);
        // If dockerfile or dockerSpec is not present ignore
        Dockerfile userDockerfile = serviceSpec.get(
                HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile), Dockerfile.class);
        if ((context.getDockerfileEntity() == null || context.getDockerfileEntity().getDockerfile() == null)
                && (userDockerfile == null)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
            return context;
        }
        try {
            dockerImageUtil.isDockerRunning();
        } catch (HyscaleException e) {
            logger.error(e.toString());
            throw e;
        }

        String appName = context.getAppName();
        String serviceName = context.getServiceName();
        boolean verbose = context.isVerbose();
        String tag = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.tag),
                String.class);
        String dockerfilePath = getDockerfilePath(userDockerfile, context);
        String dockerBuildCommand = imageCommandGenerator.dockerBuildCommand(appName, serviceName, tag, dockerfilePath,
                userDockerfile != null ? userDockerfile.getArgs() : null);

        logger.debug("Docker build command {}", dockerBuildCommand);

        String logFilePath = imageBuilderConfig.getDockerBuildlog(appName, serviceName);
        File logFile = new File(logFilePath);
        context.setBuildLogs(logFilePath);

        // TODO keep continuation activity for user
        boolean status = commandExecutor.executeInDir(dockerBuildCommand, logFile,
                userDockerfile != null ? SetupConfig.getAbsolutePath(userDockerfile.getPath()) : null);
        if (!status) {
            WorkflowLogger.endActivity(Status.FAILED);
            logger.error("Failed to build docker image");
        } else {
            WorkflowLogger.endActivity(Status.DONE);
        }

        if (verbose) {
            imageLogUtil.readBuildLogs(appName, serviceName);
        }

        if (!status) {
			throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_BUILD_IMAGE);
		}
        
        DockerImage dockerImage = new DockerImage();
        dockerImage.setName(imageCommandGenerator.getBuildImageName(appName, serviceName));
        dockerImage.setTag(tag);
        context.setDockerImage(dockerImage);

        return context;
    }


    /**
     * Get docker file path either:
     * user docker file based on dockerfile spec
     * tool generated docker file
     *
     * @param userDockerfile
     * @param context
     * @return docker file path
     */
    private String getDockerfilePath(Dockerfile userDockerfile, BuildContext context) {
        String dockerfilePath;
        if (userDockerfile != null) {
            StringBuilder sb = new StringBuilder();
            String path = userDockerfile.getPath();
            if (StringUtils.isNotBlank(path)) {
                sb.append(path).append(SetupConfig.FILE_SEPARATOR);
            }
            String dockerfileDir = userDockerfile.getDockerfilePath();
            if (StringUtils.isNotBlank(dockerfileDir)) {
                sb.append(dockerfileDir);
            }
            dockerfilePath = sb.toString();
        } else {
            dockerfilePath = context.getDockerfileEntity().getDockerfile().getParent();
        }

        return dockerfilePath;
    }

    private void validate(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        if (context == null) {
            throw new HyscaleException(ImageBuilderErrorCodes.FIELDS_MISSING, "Build Context");
        }
    }

}
