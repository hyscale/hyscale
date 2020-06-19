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
package io.hyscale.builder.services.docker.impl;

import java.io.File;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ws.rs.ProcessingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.github.dockerjava.core.command.PushImageResultCallback;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.core.models.DockerImage;
import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.builder.services.constants.DockerImageConstants;
import io.hyscale.builder.services.docker.HyscaleDockerClient;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.builder.services.spring.DockerClientCondition;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.commons.logger.ActivityContext;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.ImageRegistry;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.ImageMetadataUtil;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.Image;
import io.hyscale.servicespec.commons.util.ImageUtil;

/**
 * DockerClientImpl is docker client to docker daemon to perform all
 * image actions like pull image, tag image, push image etc.
 * It connects to the docker daemon through an environment
 * variable @see ImageBuilderConfig.getDockerHost() or through to
 * the default host based on the operating system.
 */

@Component
@Conditional(DockerClientCondition.class)
public class DockerRESTClient implements HyscaleDockerClient {

    private static final Logger logger = LoggerFactory.getLogger(DockerRESTClient.class);
    
    private static final String SHA256 = "sha256";
    
    @Autowired
    private ImageBuilderConfig imageBuilderConfig;
    
    @Autowired
    private ImageMetadataUtil imageMetadataUtil;
    
    private DefaultDockerClientConfig clientConfig;

    @PostConstruct
    public void init() {
        clientConfig
                = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(imageBuilderConfig.getDockerHost()).build();
    }
    
    public DockerClient getDockerClient() {
        return DockerClientBuilder.getInstance(clientConfig).build();
    }

    @Override
    public boolean isDockerRunning() {
        DockerClient dockerClient = getDockerClient();
        ListImagesCmd listImagesCmd = dockerClient.listImagesCmd();
        try {
            listImagesCmd.exec();
        } catch (ProcessingException | DockerException e) {
            logger.error("Error while validating isDockerRunning", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean checkForDocker() {
        DockerClient dockerClient = getDockerClient();
        VersionCmd versionCmd = dockerClient.versionCmd();
        try {
            versionCmd.exec();
        } catch (ProcessingException | DockerException e) {
            logger.error("Error while checking for docker", e);
            return false;
        }
        return true;
    }
    
    @Override
    public void deleteImages(List<String> imageIds, boolean force) {
        if (imageIds == null || imageIds.isEmpty()) {
            return;
        }
        DockerClient dockerClient = getDockerClient();
        imageIds.stream().forEach(imageId -> {
            RemoveImageCmd removeCmd = dockerClient.removeImageCmd(imageId).withForce(force);
            try {
                removeCmd.exec();
            } catch (DockerException e) {
                logger.error("Error while deleting image: {}, ignoring", imageId, e);
            }
        });
    }
    
    @Override
    public void deleteImage(String imageId, boolean force) {
        if (StringUtils.isBlank(imageId)) {
            return;
        }
        deleteImages(Arrays.asList(imageId), force);
    }

    @Override
    public DockerImage build(Dockerfile dockerfile, String tag, BuildContext buildContext) throws HyscaleException {
        ActivityContext buildActivity = new ActivityContext(ImageBuilderActivity.IMAGE_BUILD);
        WorkflowLogger.startActivity(buildActivity);
        // validate dockerfile
        try {
            validate(dockerfile);
        } catch(HyscaleException e) {
            WorkflowLogger.endActivity(buildActivity, Status.FAILED);
            logger.error("Failed to validate dockerfile before build", e);
            throw e;
        }
        String appName = buildContext.getAppName();
        String serviceName = buildContext.getServiceName();
        
        String buildImageName = imageMetadataUtil.getBuildImageNameWithTag(appName, serviceName, tag);
        BuildImageCmd buildImageCmd = getBuildCommand(dockerfile, buildImageName);
        
        String logFilePath = imageBuilderConfig.getDockerBuildlog(appName, serviceName);
        buildContext.setBuildLogs(logFilePath);
        if (buildContext.isVerbose()) {
            WorkflowLogger.header(ImageBuilderActivity.BUILD_LOGS);
        }
        BuildImageResultCallback callback = new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                try {
                    String stream = item.getStream();
                    if (stream != null) {
                        HyscaleFilesUtil.updateFile(logFilePath, stream.concat(ToolConstants.NEW_LINE));
                        if (buildContext.isVerbose()) {
                            WorkflowLogger.log(stream);
                        } else {
                            WorkflowLogger.continueActivity(buildActivity);
                        }
                    }
                } catch (HyscaleException e) {
                    logger.error("Error while writing build progress to build logs", e);
                }
                super.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                WorkflowLogger.endActivity(buildActivity, Status.FAILED);
                super.onError(throwable);
            }
        };
        try {
            buildImageCmd.exec(callback).awaitCompletion();
        } catch (DockerClientException | InterruptedException e) {
            logger.error("Failed to build image", e);
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_PUSH_IMAGE);
        }
        
        if (buildContext.isVerbose()) {
            WorkflowLogger.endActivity(Status.DONE);
            WorkflowLogger.footer();
        } else {
            WorkflowLogger.endActivity(buildActivity, Status.DONE);
        }
        DockerImage dockerImage = new DockerImage();
        dockerImage.setName(imageMetadataUtil.getBuildImageName(appName, serviceName));
        dockerImage.setTag(tag);
        return dockerImage;
    }
    
    private void validate(Dockerfile dockerfileModel) throws HyscaleException {
        if (dockerfileModel == null) {
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKERFILE_REQUIRED);
        }
        File dockerfile = getDockerFile(dockerfileModel.getDockerfilePath());
        if (dockerfile == null || !dockerfile.exists() || dockerfile.isDirectory()) {
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKERFILE_NOT_FOUND, dockerfile.getAbsolutePath());
        }
    }
    
    private BuildImageCmd getBuildCommand(Dockerfile dockerfile, String tag) {
        Set<String> tags = new HashSet<>();
        tags.add(tag);
        Map<String,String> labels = imageMetadataUtil.getImageOwnerLabel();

        DockerClient dockerClient = getDockerClient();
        BuildImageCmd buildImageCmd = dockerClient.buildImageCmd()
                .withDockerfile(getDockerFile(dockerfile.getDockerfilePath()))
                .withPull(true)
                .withNoCache(true)
                .withLabels(labels)
                .withTags(tags)
                .withTarget(dockerfile.getTarget());
        if (dockerfile.getPath() != null) {
            buildImageCmd.withDockerfilePath(dockerfile.getPath());
        }
        if (dockerfile.getArgs() != null && !dockerfile.getArgs().isEmpty()) {
            dockerfile.getArgs().entrySet().stream().forEach(each -> {
                buildImageCmd.withBuildArg(each.getKey(), each.getValue());
            });
        }
        return buildImageCmd;
    }
    
    private File getDockerFile(String dockerFilePath) {
        return new File(dockerFilePath + ToolConstants.LINUX_FILE_SEPARATOR + DockerImageConstants.DOCKERFILE_NAME);
    }

    @Override
    public void pull(String image, BuildContext context) throws HyscaleException {
        ActivityContext pullActivity = new ActivityContext(ImageBuilderActivity.IMAGE_PULL);
        WorkflowLogger.startActivity(pullActivity);
        if (StringUtils.isBlank(image)) {
            WorkflowLogger.endActivity(pullActivity, Status.SKIPPING);
            return;
        }
        DockerClient dockerClient = getDockerClient();
        AuthConfig authConfig = getAuthConfig(context.getPullRegistry());
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
        if (authConfig != null) {
            pullImageCmd.withAuthConfig(authConfig);
        }
        try {
            pullImageCmd.exec(new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    super.onNext(item);
                    WorkflowLogger.continueActivity(pullActivity);
                }
                
            }).awaitCompletion();
        } catch (DockerException | InterruptedException e) {
            logger.error("Error while pulling the image {}", image);
            WorkflowLogger.endActivity(pullActivity, Status.FAILED);
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_PULL_IMAGE, image);
        }
        WorkflowLogger.endActivity(pullActivity, Status.DONE);
    }

    @Override
    public void tag(String source, Image dest) throws HyscaleException {
        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_TAG);
        if (StringUtils.isBlank(source)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        DockerClient dockerClient = getDockerClient();
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
    public void push(Image image, BuildContext buildContext) throws HyscaleException {

        AuthConfig authConfig = getAuthConfig(buildContext.getPushRegistry());

        DockerClient dockerClient = getDockerClient();

        // Push image
        String logFilePath = imageBuilderConfig.getDockerPushLogDir(buildContext.getAppName(), buildContext.getServiceName());
        buildContext.setPushLogs(logFilePath);
        PushImageCmd pushImageCmd = dockerClient.pushImageCmd(ImageUtil.getImageWithoutTag(image)).withTag(image.getTag())
                .withAuthConfig(authConfig); 
        ActivityContext pushActivity = new ActivityContext(ImageBuilderActivity.IMAGE_PUSH);
        WorkflowLogger.startActivity(pushActivity);
        if (buildContext.isVerbose()) {
            WorkflowLogger.header(ImageBuilderActivity.IMAGE_PUSH_LOG);
        }
        PushImageResultCallback callback = new PushImageResultCallback() {
            @Override
            public void onNext(PushResponseItem item) {
                try {
                    String status = item.getStatus();
                    if (status != null) {
                        HyscaleFilesUtil.updateFile(logFilePath, status.concat(ToolConstants.NEW_LINE));
                        if (buildContext.isVerbose()) {
                            WorkflowLogger.log(status);
                        } else {
                            WorkflowLogger.continueActivity(pushActivity);
                        }
                        // From the push item we should be able to get SHAID?
                        if (status.contains(SHA256)) {
                            buildContext.setImageShaSum(getImageDigest(status));
                        }
                    }
                } catch (HyscaleException e) {
                    logger.error("Error while writing push progress to push logs", e);
                }
                super.onNext(item);
            }

            @Override
            public void onError(Throwable throwable) {
                WorkflowLogger.endActivity(pushActivity, Status.FAILED);
                //WorkflowLogger.error(ImageBuilderActivity.FAILED_TO_PUSH_IMAGE, image.getName(), throwable.getMessage());
                super.onError(throwable);
            }
        };

        try {
            pushImageCmd.exec(callback).awaitCompletion();
        } catch (DockerClientException | InterruptedException e) {
            logger.error("Failed to push image {}", image.getName(), e);
            throw new HyscaleException(ImageBuilderErrorCodes.FAILED_TO_PUSH_IMAGE);
        }
        if (buildContext.isVerbose()) {
            WorkflowLogger.endActivity(Status.DONE);
            WorkflowLogger.footer();
        } else {
            WorkflowLogger.endActivity(pushActivity, Status.DONE);
        }
    }
    
    private AuthConfig getAuthConfig(ImageRegistry imageRegistry) {
        if (imageRegistry == null) {
            return null;
        }
        AuthConfig authConfig = new AuthConfig();
        authConfig.withRegistryAddress(imageRegistry.getUrl());

        String decodedAuth = new String(Base64.getDecoder().decode(imageRegistry.getToken()));
        String[] credentialArr = decodedAuth.split(":");
        if (credentialArr.length >= 2) {
            authConfig.withUsername(credentialArr[0]);
            authConfig.withPassword(credentialArr[1]);
        }
        //authConfig.withAuth(buildContext.getImageRegistry().getToken());
        return authConfig;
    }
    
    private String getImageDigest(String status) {
        Optional<String> digest = Arrays.asList(status.split(" ")).stream().filter(each -> each.contains(SHA256))
                .findFirst();
        if (digest.isPresent()) {
            return digest.get().trim();
        }
        return null;
    }

    @Override
    public List<String> getImageIds(String imageName, Map<String, String> labels) throws HyscaleException {
        ListImagesCmd listImageCmd = getDockerClient().listImagesCmd();
        if (StringUtils.isNotBlank(imageName)) {
            listImageCmd.withImageNameFilter(imageName);
        }
        if (labels != null) {
            listImageCmd.withLabelFilter(labels);
        }

        List<com.github.dockerjava.api.model.Image> imageList = listImageCmd.exec();
        if (imageList == null || imageList.isEmpty()) {
            logger.debug("No images found to clean from the host machine");
            return null;
        }
        return imageList.stream().map(image -> {
            return image.getId();
        }).collect(Collectors.toCollection(LinkedList::new));
    }

}
