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
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
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
import io.hyscale.builder.services.docker.HyscaleDockerClient;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.builder.services.predicates.ImageBuilderPredicates;
import io.hyscale.builder.services.provider.StackImageProvider;
import io.hyscale.builder.services.service.ImageBuildPushService;
import io.hyscale.builder.services.util.DockerfileUtil;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.framework.events.model.ActivityState;
import io.hyscale.commons.framework.events.publisher.EventPublisher;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.ImageRegistry;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.ImageMetadataProvider;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.Image;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.predicates.ServiceSpecPredicates;
import io.hyscale.servicespec.commons.util.ImageUtil;

@Component
public class LocalImageBuildPushServiceImpl implements ImageBuildPushService {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageBuildPushServiceImpl.class);

    @Autowired
    private HyscaleDockerClient hyscaleDockerClient;

    @Autowired
    private ImageBuilderConfig imageBuilderConfig;
    
    @Autowired
    private ImageMetadataProvider imageMetadataProvider;

    @Autowired
    private ImageCleanUpProcessor imageCleanUp;

    @Autowired
    private EventPublisher publisher;
    
    @Autowired
    private DockerfileUtil dockerfileUtil;
    
    @Autowired
    private StackImageProvider stackImageProvider;

    @Override
    public void buildAndPush(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        validate(serviceSpec, context);
        if (!ImageBuilderPredicates.getBuildPushRequiredPredicate().test(serviceSpec, context)) {
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
        
        Map<String, ImageRegistry> registryMap = context.getRegistriesMap();
        if (hyscaleDockerClient.isLoginRequired()) {
            for (ImageRegistry imageRegistry : registryMap.values()) {
                hyscaleDockerClient.login(imageRegistry);
            }
        }
        updateContext(serviceSpec, context);
            
        try {
            buildImage(serviceSpec, registryMap, context);
            
            String sourceImage = getSourceImageName(serviceSpec, context);
            
            if (context.isStackAsServiceImage().booleanValue()) {
                pullImage(sourceImage, serviceSpec, registryMap);
            }
            
            Image image = serviceSpec.get(HyscaleSpecFields.image, Image.class);
            
            tagImage(sourceImage, image);
            
            pushImage(image, serviceSpec, context);
        } finally {
            if (hyscaleDockerClient.isCleanUpRequired()) {
                logger.debug("Cleaning up temporary config");
                // Clean up temp config
                HyscaleFilesUtil.deleteDirectory(SetupConfig.getTemporaryDockerConfigDir());
            }
        }

        // Clean up images based on clean up policy
        imageCleanUp.cleanUp(serviceSpec, context);
    }

    private void updateContext(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        if (context.isStackAsServiceImage() == null) {
            // Check if stack as service image condition
            context.setStackAsServiceImage(ServiceSpecPredicates.stackAsServiceImage().test(serviceSpec));
        }
        if (StringUtils.isEmpty(context.getServiceName())) {
            context.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        }
    }

    private ImageRegistry getStackImagePullRegistry(ServiceSpec serviceSpec, Map<String, ImageRegistry> registryMap) {
        String stackImage = stackImageProvider.getStackImageFromBuildSpec(serviceSpec);
        if (stackImage != null) {
            return registryMap.get(stackImage.split("/")[0]);
        }
        return null;
    }

    private void buildImage(ServiceSpec serviceSpec, Map<String, ImageRegistry> registryMap, BuildContext context)
            throws HyscaleException {
        Dockerfile userDockerfile = serviceSpec.get(
                HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile), Dockerfile.class);
        //Skip Image Build if neither dockerfile from buildSpec nor user dockerfile is available
        if (ImageBuilderPredicates.getSkipBuildPredicate().test(userDockerfile, context)) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_BUILD);
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        String imageName = imageMetadataProvider.getBuildImageName(context.getAppName(), context.getServiceName());
        String tag = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.tag),
                String.class);
        //Prepare Dockerfile for Image build
        Dockerfile dockerfile = getDockerfile(userDockerfile, context);
        ImageBuildEvent event = new ImageBuildEvent(ActivityState.STARTED, new File(dockerfile.getDockerfilePath()));
        publisher.publishEvent(event);
        String logFilePath = imageBuilderConfig.getDockerBuildlog(context.getAppName(), context.getServiceName());
        try {
            DockerImage dockerImage = hyscaleDockerClient.build(dockerfile, imageName, tag, registryMap, logFilePath,
                    context.isVerbose());
            context.setDockerImage(dockerImage);
            event = new ImageBuildEvent(ActivityState.DONE, new File(dockerfile.getDockerfilePath()), logFilePath);
        } catch (HyscaleException ex) {
            event = new ImageBuildEvent(ActivityState.FAILED, new File(dockerfile.getDockerfilePath()), logFilePath);
            throw ex;
        } finally {
            publisher.publishEvent(event);
            context.setBuildLogs(logFilePath);
        }
    }

    /**
     *  Only need to pull in case of stack as service image for tagging
     *  In other cases docker build will internally take care of pulling the image
     *  
     * @param sourceImage
     * @param registryMap 
     * @param context
     * @throws HyscaleException
     */
    private void pullImage(String sourceImage, ServiceSpec serviceSpec, Map<String, ImageRegistry> registryMap)
            throws HyscaleException {
        ImageRegistry pullRegistry = getStackImagePullRegistry(serviceSpec, registryMap);
        ImagePullEvent event = new ImagePullEvent(ActivityState.STARTED, sourceImage);
        publisher.publishEvent(event);

        try {
            hyscaleDockerClient.pull(sourceImage, pullRegistry);
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

    private void pushImage(Image image, ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        String registryUrl = serviceSpec
                .get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class);
        ImageRegistry pushRegistry = context.getRegistriesMap().get(registryUrl);
        if (pushRegistry == null) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PUSH);
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        String logFilePath = imageBuilderConfig.getDockerPushLogDir(context.getAppName(), context.getServiceName());
        String pushImage = ImageUtil.getImage(image);
        ImagePushEvent event = new ImagePushEvent(ActivityState.STARTED, pushImage, logFilePath);
        publisher.publishEvent(event);
        try {
            String shaSum = hyscaleDockerClient.push(image, pushRegistry, logFilePath, context.isVerbose());
            context.setImageShaSum(shaSum);
            event = new ImagePushEvent(ActivityState.DONE, pushImage, logFilePath);
        } catch (HyscaleException e) {
            event = new ImagePushEvent(ActivityState.FAILED, pushImage, logFilePath);
            throw e;
        } finally {
            publisher.publishEvent(event);
            context.setPushLogs(logFilePath);
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
            dockerfilePath = dockerfileUtil.getDockerfileAbsolutePath(userDockerfile);
        } else {
            dockerfilePath = context.getDockerfileEntity().getDockerfile().getAbsolutePath();
        }

        return dockerfilePath;
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

        if (BooleanUtils.toBoolean(buildContext.isStackAsServiceImage())) {
            return serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec,
                    HyscaleSpecFields.stackImage), String.class);
        }
        DockerImage dockerImage = buildContext.getDockerImage();

        return StringUtils.isNotBlank(dockerImage.getTag())
                ? dockerImage.getName() + ToolConstants.COLON + dockerImage.getTag()
                : dockerImage.getName();
    }

    private boolean validate(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        String imageName = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.name),
                String.class);
        if (StringUtils.isBlank(imageName)) {
            throw new HyscaleException(ImageBuilderErrorCodes.CANNOT_RESOLVE_IMAGE_NAME);
        }
        String registryUrl = serviceSpec
                .get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class);
        if (registryUrl != null && context.getRegistriesMap().get(registryUrl) == null) {
            throw new HyscaleException(ImageBuilderErrorCodes.MISSING_DOCKER_REGISTRY_CREDENTIALS, registryUrl,
                    registryUrl);
        }
        return true;
    }

    private void validateDockerfilePath(String dockerfilePath) throws HyscaleException {
        if (!ImageBuilderPredicates.getDockerfileExistsPredicate().test(dockerfilePath)) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_BUILD);
            WorkflowLogger.endActivity(Status.FAILED);
            File dockerfile = new File(dockerfilePath);
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKERFILE_NOT_FOUND, dockerfile.getAbsolutePath());
        }
    }

}
