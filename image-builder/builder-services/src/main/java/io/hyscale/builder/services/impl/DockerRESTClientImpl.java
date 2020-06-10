/**
 * Copyright 2019 Pramati Prism, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.builder.services.impl;

import com.fasterxml.jackson.databind.ser.std.CollectionSerializer;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;
import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.core.models.DockerImage;
import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.builder.services.service.ImageBuilder;
import io.hyscale.builder.services.spring.DockerClientCondition;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
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
import java.io.File;
import java.util.*;

/**
 * DockerClientImpl is docker client to docker daemon to perform all
 * image actions like pull image, tag image, push image etc.
 * It connects to the docker daemon through an environment
 * variable @see ImageBuilderConfig.getDockerHost() or through to
 * the default host based on the operating system.
 */

@Conditional(DockerClientCondition.class)
@Component
public class DockerRESTClientImpl implements ImageBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DockerRESTClientImpl.class);

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
    public boolean isDockerRunning() {
        DockerClient dockerClient = DockerClientBuilder.getInstance(clientConfig).build();
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        try {
            listImagesCmd.exec();
        } catch (DockerException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean checkForDocker() {
        DockerClient dockerClient = DockerClientBuilder.getInstance(clientConfig).build();
        VersionCmd versionCmd = dockerClient.versionCmd();
        try {
            versionCmd.exec();
        } catch (DockerException e) {
            return false;
        }
        return true;
    }

    @Override
    public DockerImage _build(Dockerfile dockerfile, String tag, BuildContext context) throws HyscaleException {
        // validate dockerfile
        Set<String> tags = new HashSet<>();
        tags.add(tag);
        Map<String,String> labels = new HashMap<>();
        labels.put("imageowner","hyscale");

        DockerClient dockerClient = DockerClientBuilder.getInstance(clientConfig).build();
        BuildImageCmd buildImageCmd=dockerClient.buildImageCmd()
                .withDockerfile(new File(dockerfile.getDockerfilePath()))
                .withDockerfilePath(dockerfile.getPath())
                .withPull(true)
                .withNoCache(true)
                .withLabels(labels)
                .withTags(tags)
                .withTarget(dockerfile.getTarget());
        if(dockerfile.getArgs()!=null && !dockerfile.getArgs().isEmpty()){
            dockerfile.getArgs().entrySet().stream().forEach(each->{
                buildImageCmd.withBuildArg(each.getKey(),each.getValue());
            });
        }

        String imageid= buildImageCmd.exec(new BuildImageResultCallback()).awaitImageId();
        DockerImage dockerImage = new DockerImage();
        dockerImage.setName(imageCommandProvider.getBuildImageName(appName, serviceName));
        dockerImage.setTag(tag);


        return null;
    }

    @Override
    public void _pull(String image, BuildContext context) throws HyscaleException {
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PULL);
        if (StringUtils.isBlank(image)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }

        //TODO pull with authConfig, read the pull registry credentials from build context/ imagemanager
        DockerClient dockerClient = DockerClientBuilder.getInstance(clientConfig).build();
        try {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            pullImageCmd.exec(new PullImageResultCallback() {
            }).awaitSuccess();
        } catch (DockerException e) {
            logger.error("Error while pulling the image {}", image);
            WorkflowLogger.endActivity(Status.FAILED);
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_PULL_IMAGE, image);
        }
        WorkflowLogger.endActivity(Status.DONE);
    }

    @Override
    public void _tag(String source, Image dest) throws HyscaleException {
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_TAG);
        if (StringUtils.isBlank(source)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        DockerClient dockerClient = DockerClientBuilder.getInstance(clientConfig).build();
        try {
            TagImageCmd tagImageCmd = dockerClient.tagImageCmd(source, ImageUtil.getImageWithoutTag(dest), dest.getTag());
            tagImageCmd.exec();
        } catch (DockerException e) {
            WorkflowLogger.endActivity(Status.FAILED);
            logger.error(e.toString());
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_TAG_IMAGE);
        }
        WorkflowLogger.endActivity(Status.DONE);
    }


    @Override
    public void _push(Image image, BuildContext buildContext) throws HyscaleException {

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

        // Push image
        String logFilePath = imageBuilderConfig.getDockerPushLogDir(buildContext.getAppName(), buildContext.getServiceName());
        buildContext.setPushLogs(logFilePath);
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
}
