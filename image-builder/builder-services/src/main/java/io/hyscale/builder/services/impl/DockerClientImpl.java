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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.command.TagImageCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.core.models.ImageBuilder;
import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.builder.services.service.ImagePushService;
import io.hyscale.builder.services.spring.DockerClientCondition;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Image;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import com.github.dockerjava.core.DefaultDockerClientConfig;

import javax.annotation.PostConstruct;
import java.util.Base64;

/**
 * DockerClientImpl is docker client to docker daemon to perform all
 * image actions like pull image, tag image, push image etc.
 * It connects to the docker daemon through an environment
 * variable @see ImageBuilderConfig.getDockerHost() or through to
 * the default host based on the operating system.
 */

@Conditional(DockerClientCondition.class)
@Component
public class DockerClientImpl implements ImagePushService {

    private static final Logger logger = LoggerFactory.getLogger(DockerClientImpl.class);

    @Autowired
    private ImageBuilderConfig imageBuilderConfig;

    private DefaultDockerClientConfig clientConfig;


    @PostConstruct
    public void init() {
        clientConfig
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(imageBuilderConfig.getDockerHost()).build();
    }

    @Override
    public void _push(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {

        AuthConfig authConfig = new AuthConfig();
        authConfig.withRegistryAddress(buildContext.getImageRegistry().getUrl());

        String decodedAuth = new String(Base64.getDecoder().decode(buildContext.getImageRegistry().getToken()));
        String[] credentialArr = decodedAuth.split(":");
        if (credentialArr.length >= 2) {
            authConfig.withUsername(credentialArr[0]);
            authConfig.withPassword(credentialArr[1]);
        }
        //authConfig.withAuth(buildContext.getImageRegistry().getToken());

        DockerClient dockerClient = DockerClientBuilder.getInstance(clientConfig).build();

        // Check for if Docker Daemon running
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        try {
            listImagesCmd.exec();
        } catch (DockerException e) {
            WorkflowLogger.error(ImageBuilderActivity.DOCKER_DAEMON_NOT_RUNNING);
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKER_DAEMON_NOT_RUNNING);
        }

        String sourceImage = getSourceImageName(serviceSpec, buildContext);
        Image image = serviceSpec.get(HyscaleSpecFields.image, Image.class);

        // if stackAsServiceImage
        if (buildContext.isStackAsServiceImage()) {
            // pull the image
            pullStackImage(dockerClient, sourceImage);
        }

        // Tag Image
        tagImage(dockerClient, sourceImage, image);


        // Push image
        String logFilePath = imageBuilderConfig.getDockerPushLogDir(buildContext.getAppName(), buildContext.getServiceName());
        buildContext.setPushLogs(logFilePath);
        logger.debug("~~~~~~~{} , {}, {} ~~~~~~~~~~~~~~", authConfig.getRegistryAddress(), authConfig.getUsername(), authConfig.getPassword());
        PushImageCmd pushImageCmd = dockerClient.pushImageCmd(ImageUtil.getImageWithoutTag(image)).withTag(image.getTag())
                .withAuthConfig(authConfig);
        if (buildContext.isVerbose()) {
            WorkflowLogger.header(ImageBuilderActivity.IMAGE_PUSH_LOG);
        }
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PUSH);
        PushImageResultCallback callback = new PushImageResultCallback() {
            @Override
            public void onNext(PushResponseItem item) {
                try {
                    HyscaleFilesUtil.updateFile(logFilePath, item.getStatus());
                    if (buildContext.isVerbose()) {
                        WorkflowLogger.write(item.getStatus());
                    }
                } catch (HyscaleException e) {
                    logger.error("Error while writing push progress to push logs", e);
                }
                super.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                WorkflowLogger.endActivity(Status.FAILED);
                //WorkflowLogger.error(ImageBuilderActivity.FAILED_TO_PUSH_IMAGE, image.getName(), throwable.getMessage());
                super.onError(throwable);
            }
        };

        try {
            pushImageCmd.exec(callback).awaitSuccess();
        } catch (DockerClientException e) {
            logger.error("Failed to push image {}", image.getName(), e);
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_PUSH_IMAGE);
        }
        WorkflowLogger.endActivity(Status.DONE);
        if (buildContext.isVerbose()) {
            WorkflowLogger.footer();
        }
    }

    private void tagImage(DockerClient dockerClient, String sourceImage, Image image) throws HyscaleException {
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_TAG);
        if (StringUtils.isBlank(sourceImage)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        try {
            TagImageCmd tagImageCmd = dockerClient.tagImageCmd(sourceImage, ImageUtil.getImageWithoutTag(image), image.getTag());

            tagImageCmd.exec();
        } catch (DockerException e) {
            WorkflowLogger.endActivity(Status.FAILED);
            logger.error(e.toString());
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_TAG_IMAGE);
        }
        WorkflowLogger.endActivity(Status.DONE);
    }

    private void pullStackImage(DockerClient dockerClient, String imageName) throws HyscaleException {
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PULL);
        if (StringUtils.isBlank(imageName)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }

        //TODO pull with authConfig, read the pull registry credentials from build context/ imagemanager
        try {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(imageName);
            pullImageCmd.exec(new PullImageResultCallback() {
            }).awaitSuccess();
        } catch (DockerException e) {
            logger.error("Error while pulling the image {}", imageName);
            WorkflowLogger.endActivity(Status.FAILED);
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_PULL_IMAGE, imageName);
        }
        WorkflowLogger.endActivity(Status.DONE);
    }
}
