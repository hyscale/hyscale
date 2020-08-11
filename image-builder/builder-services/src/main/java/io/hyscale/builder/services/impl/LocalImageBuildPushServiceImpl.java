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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.core.models.DockerImage;
import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.builder.events.model.ImageBuildEvent;
import io.hyscale.builder.events.model.ImagePullEvent;
import io.hyscale.builder.events.model.ImagePushEvent;
import io.hyscale.builder.events.model.ImageTagEvent;
import io.hyscale.builder.services.cleanup.ImageCleanUpProcessor;
import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.builder.services.constants.DockerImageConstants;
import io.hyscale.builder.services.docker.HyscaleDockerClient;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.builder.services.service.ImageBuildPushService;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.framework.events.model.ActivityState;
import io.hyscale.commons.framework.events.publisher.EventPublisher;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.Image;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.util.ImageUtil;

@Component
public class LocalImageBuildPushServiceImpl implements ImageBuildPushService {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageBuildPushServiceImpl.class);

    @Autowired
    private HyscaleDockerClient hyscaleDockerClient;

    @Autowired
    private ImageBuilderConfig imageBuilderConfig;

    @Autowired
    private ImageCleanUpProcessor imageCleanUp;

    @Autowired
    private EventPublisher publisher;

    @Override
    public void buildAndPush(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        validate(serviceSpec, context);
        if (!isImageBuildPushRequired(serviceSpec, context)) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_BUILD_PUSH);
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }

        // Check if docker is installed or not
        if (!hyscaleDockerClient.checkForDocker()) {
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKER_NOT_INSTALLED);
        }

        if (!hyscaleDockerClient.isDockerRunning()) {
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKER_DAEMON_NOT_RUNNING);
        }

        buildImage(serviceSpec, context);

        String sourceImage = getSourceImageName(serviceSpec, context);

        pullImage(sourceImage, context);

        Image image = serviceSpec.get(HyscaleSpecFields.image, Image.class);

        tagImage(sourceImage, image);

        pushImage(image, context);

        // Clean up images based on clean up policy
        imageCleanUp.cleanUp(serviceSpec, context);
    }

    private void buildImage(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        Dockerfile userDockerfile = serviceSpec.get(
                HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile), Dockerfile.class);
        //Skip Image Build if neither dockerfile from buildSpec nor user dockerfile is available
        if (skipBuild(userDockerfile, context)) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_BUILD);
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        String tag = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.tag),
                String.class);
        //Prepare Dockerfile for Image build
        Dockerfile dockerfile = getDockerfile(userDockerfile, context);
        ImageBuildEvent event = new ImageBuildEvent(ActivityState.STARTED, new File(dockerfile.getDockerfilePath()));
        publisher.publishEvent(event);
        String logFilePath = imageBuilderConfig.getDockerBuildlog(context.getAppName(), context.getServiceName());
        try {
            DockerImage dockerImage = hyscaleDockerClient.build(dockerfile, tag, context);
            context.setDockerImage(dockerImage);
            event = new ImageBuildEvent(ActivityState.DONE, new File(dockerfile.getDockerfilePath()), logFilePath);
        } catch (HyscaleException ex) {
            event = new ImageBuildEvent(ActivityState.FAILED, new File(dockerfile.getDockerfilePath()), logFilePath);
            throw ex;
        } finally {
            publisher.publishEvent(event);
        }
    }

    private void pullImage(String sourceImage, BuildContext context) throws HyscaleException {
        if (!context.isStackAsServiceImage()) {
            return;
        }
        ImagePullEvent event = new ImagePullEvent(ActivityState.STARTED, sourceImage);
        publisher.publishEvent(event);
        try {
            hyscaleDockerClient.pull(sourceImage, context);
            event = new ImagePullEvent(ActivityState.DONE, sourceImage);
        } catch (HyscaleException ex) {
            event = new ImagePullEvent(ActivityState.FAILED, sourceImage);
            throw ex;
        } finally {
            publisher.publishEvent(event);
        }
    }

    private void tagImage(String sourceImage, Image image) throws HyscaleException {
        if (StringUtils.isBlank(sourceImage)) {
            return;
        }
        String destinationImage = ImageUtil.getImage(image);
        ImageTagEvent event = new ImageTagEvent(ActivityState.STARTED, sourceImage, destinationImage);
        publisher.publishEvent(event);
        try {
            hyscaleDockerClient.tag(sourceImage, image);
            event = new ImageTagEvent(ActivityState.DONE, sourceImage, destinationImage);
        } catch (HyscaleException e) {
            event = new ImageTagEvent(ActivityState.FAILED, sourceImage, destinationImage);
            throw e;
        } finally {
            publisher.publishEvent(event);
        }
    }

    private void pushImage(Image image, BuildContext context) throws HyscaleException {
        String logFilePath = imageBuilderConfig.getDockerPushLogDir(context.getAppName(), context.getServiceName());
        String pushImage = ImageUtil.getImage(image);
        ImagePushEvent event = new ImagePushEvent(ActivityState.STARTED, pushImage, logFilePath);
        publisher.publishEvent(event);
        if (context.getPushRegistry() == null) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PUSH);
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        try {
            hyscaleDockerClient.push(image, context);
            event = new ImagePushEvent(ActivityState.DONE, pushImage, logFilePath);
        } catch (HyscaleException e) {
            event = new ImagePushEvent(ActivityState.FAILED, pushImage, logFilePath);
            throw e;
        } finally {
            publisher.publishEvent(event);
        }
    }

    private Dockerfile getDockerfile(Dockerfile userDockerfile, BuildContext context) throws HyscaleException {
        Dockerfile dockerfile = new Dockerfile();
        String dockerfilePath = getDockerfilePath(userDockerfile, context);
        validateDockerfilePath(dockerfilePath);
        dockerfile.setDockerfilePath(dockerfilePath);
        dockerfile.setArgs(userDockerfile != null ? userDockerfile.getArgs() : null);
        dockerfile.setTarget(userDockerfile != null ? userDockerfile.getTarget() : null);
        dockerfile.setPath(userDockerfile != null ? SetupConfig.getAbsolutePath(userDockerfile.getPath()) : null);
        return dockerfile;
    }

    /**
     * Get docker file path either:
     * User docker file based on dockerfile in spec or
     * Tool generated dockerfile
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
                sb.append(path);
                if (!path.endsWith(ToolConstants.FILE_SEPARATOR)) {
                    sb.append(ToolConstants.FILE_SEPARATOR);
                }
            }
            String dockerfileDir = userDockerfile.getDockerfilePath();
            if (StringUtils.isNotBlank(dockerfileDir)) {
                sb.append(dockerfileDir);
            }
            dockerfilePath = sb.toString();
            dockerfilePath = StringUtils.isNotBlank(dockerfilePath) ? SetupConfig.getAbsolutePath(dockerfilePath)
                    : SetupConfig.getAbsolutePath(".");
        } else {
            dockerfilePath = context.getDockerfileEntity().getDockerfile().getParent();
        }

        return dockerfilePath;
    }

    /**
     * Not required if dockerSpec and dockerfile are not available.
     * In case its just a stack image, need to push only
     *
     * @param serviceSpec
     * @param context
     * @return boolean
     * @throws HyscaleException
     */
    private boolean isImageBuildPushRequired(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {

        if (context.isStackAsServiceImage()) {
            return true;
        }

        // No dockerfile
        return (serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile),
                Dockerfile.class) != null)
                || (context.getDockerfileEntity() != null && context.getDockerfileEntity().getDockerfile() != null);
    }

    /**
     * Source image is either the stack image being used as service image or
     * Local image build through dockerfile
     *
     * @param serviceSpec
     * @param buildContext
     * @return source image name
     * @throws HyscaleException
     */
    public String getSourceImageName(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {

        if (buildContext.isStackAsServiceImage()) {
            return serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec,
                    HyscaleSpecFields.stackImage), String.class);
        }
        DockerImage dockerImage = buildContext.getDockerImage();

        return StringUtils.isNotBlank(dockerImage.getTag())
                ? dockerImage.getName() + ToolConstants.COLON + dockerImage.getTag()
                : dockerImage.getName();
    }

    public boolean skipBuild(Dockerfile userDockerfile, BuildContext context) {
        return ((context.getDockerfileEntity() == null || context.getDockerfileEntity().getDockerfile() == null)
                && (userDockerfile == null));
    }


    private boolean validate(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        String imageName = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.name),
                String.class);
        if (StringUtils.isBlank(imageName)) {
            throw new HyscaleException(ImageBuilderErrorCodes.CANNOT_RESOLVE_IMAGE_NAME);
        }
        String registryUrl = serviceSpec
                .get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class);
        if (context.getPushRegistry() == null && registryUrl != null) {
            throw new HyscaleException(ImageBuilderErrorCodes.MISSING_DOCKER_REGISTRY_CREDENTIALS, registryUrl,
                    registryUrl);
        }
        return true;
    }

    private void validateDockerfilePath(String dockerfilePath) throws HyscaleException {
        File dockerfile = new File(
                dockerfilePath + ToolConstants.LINUX_FILE_SEPARATOR + DockerImageConstants.DOCKERFILE_NAME);
        if (!dockerfile.exists() || dockerfile.isDirectory()) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_BUILD);
            WorkflowLogger.endActivity(Status.FAILED);
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKERFILE_NOT_FOUND, dockerfile.getAbsolutePath());
        }
    }

}
