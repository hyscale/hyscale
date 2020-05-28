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
import java.io.IOException;
import java.util.List;

import io.hyscale.builder.services.spring.DockerBinaryCondition;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.builder.services.service.ImagePushService;
import io.hyscale.builder.services.util.DockerImageUtil;
import io.hyscale.builder.services.util.ImageLogUtil;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.CommandResult;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.util.ImageUtil;

@Conditional(DockerBinaryCondition.class)
@Component
public class DockerBinaryPushService implements ImagePushService {

    private static final Logger logger = LoggerFactory.getLogger(DockerBinaryPushService.class);

    @Autowired
    private DockerImageUtil dockerImageUtil;

    @Autowired
    private ImageCommandProvider commandGenerator;

    @Autowired
    private ImageLogUtil imageLogUtil;

    @Autowired
    private ImageBuilderConfig imageBuilderConfig;

    /**
     * Check docker exists, If stack image as service image pull, tag
     * Push image if required else return
     *
     * @throws HyscaleException
     */
    @Override
    public void _push(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {

        validate(serviceSpec, buildContext);

        try {
            dockerImageUtil.isDockerRunning();
        } catch (HyscaleException e) {
            logger.error(e.toString());
            throw e;
        }
        String imageFullPath = ImageUtil.getImage(serviceSpec);
        String sourceImage = getSourceImageName(serviceSpec, buildContext);

        if (buildContext.isStackAsServiceImage()) {
            pullImage(sourceImage);
        }
        tagImage(sourceImage, imageFullPath);
		if (buildContext.getImageRegistry() == null) {
			WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PUSH);
			WorkflowLogger.endActivity(Status.SKIPPING);
			return;
		}
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PUSH);
        String appName = buildContext.getAppName();
        String serviceName = buildContext.getServiceName();
        boolean verbose = buildContext.isVerbose();
        String pushImageCommand = commandGenerator.dockerPush(imageFullPath);
        String logFilePath = imageBuilderConfig.getDockerPushLogDir(appName, serviceName);
        File logFile = new File(logFilePath);
        buildContext.setPushLogs(logFilePath);
        // TODO keep continuation activity for user , launch a new thread & waitFor
        boolean status = CommandExecutor.execute(pushImageCommand, logFile);
        if (!status) {
            WorkflowLogger.endActivity(Status.FAILED);
            logger.error("Failed to push docker image");
        } else {
            String inspectCommand = commandGenerator.dockerInspect(ImageUtil.getImage(serviceSpec));
            CommandResult result = CommandExecutor.executeAndGetResults(inspectCommand);
            buildContext.setImageShaSum(getImageDigest(result));
            WorkflowLogger.endActivity(Status.DONE);
        }

        if (verbose) {
            imageLogUtil.readPushLogs(appName, serviceName);
        }

        if (!status) {
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_PUSH_IMAGE);
        }

    }

    private void pullImage(String imageName) throws HyscaleException {
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PULL);

        if (StringUtils.isBlank(imageName)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        try {
            dockerImageUtil.pullImage(imageName);
        } catch (HyscaleException e) {
            WorkflowLogger.endActivity(Status.FAILED);
            logger.error(e.toString());
            throw e;
        }
        WorkflowLogger.endActivity(Status.DONE);

    }

    private void tagImage(String sourceImage, String targetImage) throws HyscaleException {
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_TAG);

        if (StringUtils.isBlank(sourceImage)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        try {
            dockerImageUtil.tagImage(sourceImage, targetImage);
        } catch (HyscaleException e) {
            WorkflowLogger.endActivity(Status.FAILED);
            logger.error(e.toString());
            throw e;
        }
        WorkflowLogger.endActivity(Status.DONE);

    }

    /*
     * If only stack image return stackImageName else return dockerImage name
     */


    /**
     * Gets latest digest from inspect image command result.
     *
     * @param result CommandResult obtained after executing docker inspect command.
     *  1.result is null  - null
     *  2.result not null - null if no digests
     *                    - last digest if digests exist
     * @return digest latest digest from image command result.
     */
    private String getImageDigest(CommandResult result) {
        if (result == null || result.getExitCode() > 0 || StringUtils.isBlank(result.getCommandOutput())) {
            return null;
        }
        ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
        try {
            JsonNode node = mapper.readTree(result.getCommandOutput());
            JsonNode digestNode = null;
            if (node.isArray()) {
                digestNode = node.get(0).get("RepoDigests");
            } else {
                digestNode = node.get("RepoDigests");
            }
            if (digestNode == null) {
                return null;
            }
            List<String> digestList = mapper.convertValue(digestNode, new TypeReference<List<String>>() {
            });
            String latestRepoDigest =  digestList.get(digestList.size() - 1);
            if (StringUtils.isNotBlank(latestRepoDigest) && latestRepoDigest.contains(ToolConstants.AT_SIGN)) {
                 return latestRepoDigest.split(ToolConstants.AT_SIGN)[1];
            }
        } catch (IOException e) {
            logger.debug("Error while processing image inspect results ", e);
        }
        return null;
    }


}
