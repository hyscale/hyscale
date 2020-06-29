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
import io.hyscale.builder.services.cleanup.ImageCleanUpProcessor;
import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.builder.services.constants.DockerImageConstants;
import io.hyscale.builder.services.docker.HyscaleDockerClient;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.builder.services.service.ImageBuildPushService;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.Image;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class LocalImageBuildPushServiceImpl implements ImageBuildPushService {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageBuildPushServiceImpl.class);

    @Autowired
    private HyscaleDockerClient hyscaleDockerClient;
    
    @Autowired
    private ImageCleanUpProcessor imageCleanUp;

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
        
        //TEST without error message
        if (!hyscaleDockerClient.isDockerRunning()) {
            //WorkflowLogger.error(ImageBuilderActivity.DOCKER_DAEMON_NOT_RUNNING);
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKER_DAEMON_NOT_RUNNING);
        }
        Dockerfile userDockerfile = serviceSpec.get(
                HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile), Dockerfile.class);
        //Skip Image Build if neither dockerfile from buildSpec nor user dockerfile is available
        if (skipBuild(userDockerfile, context)) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_BUILD);
            WorkflowLogger.endActivity(Status.SKIPPING);
        } else {
            DockerImage dockerImage = null;
            String tag = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.tag),
                    String.class);
            //Prepare Dockerfile for Image build
            Dockerfile dockerfile = new Dockerfile();
            String dockerfilePath = getDockerfilePath(userDockerfile, context);
            validateDockerfilePath(dockerfilePath);
            dockerfile.setDockerfilePath(dockerfilePath);
            dockerfile.setArgs(userDockerfile != null ? userDockerfile.getArgs() : null);
            dockerfile.setTarget(userDockerfile != null ? userDockerfile.getTarget() : null);
            dockerfile.setPath(userDockerfile != null ? userDockerfile.getPath() : null);
            dockerImage = hyscaleDockerClient.build(dockerfile, tag, context);
            context.setDockerImage(dockerImage);
        }
        // validate Push
        validate(serviceSpec, context);
        String sourceImage = getSourceImageName(serviceSpec, context);

        if (context.isStackAsServiceImage()) {
            hyscaleDockerClient.pull(sourceImage, context);
        }
        Image image = serviceSpec.get(HyscaleSpecFields.image, Image.class);
        hyscaleDockerClient.tag(sourceImage, image);

        if (context.getPushRegistry() == null) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PUSH);
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }
        hyscaleDockerClient.push(image, context);
        
        // Clean up images based on clean up policy
        imageCleanUp.cleanUp(serviceSpec, context);
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
    public String getDockerfilePath(Dockerfile userDockerfile, BuildContext context) {
        String dockerfilePath;
        if (userDockerfile != null) {
            StringBuilder sb = new StringBuilder();
            String path = userDockerfile.getPath();
            if (StringUtils.isNotBlank(path)) {
                sb.append(path).append(ToolConstants.FILE_SEPARATOR);
            }
            String dockerfileDir = userDockerfile.getDockerfilePath();
            if (StringUtils.isNotBlank(dockerfileDir)) {
                sb.append(dockerfileDir);
            }
            dockerfilePath = sb.toString();
            dockerfilePath = StringUtils.isNotBlank(dockerfilePath) ? dockerfilePath : SetupConfig.getAbsolutePath(".");
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
        if ((context.getDockerfileEntity() == null || context.getDockerfileEntity().getDockerfile() == null)
                && (serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile),
                Dockerfile.class) == null)) {
            return false;
        }

        return true;
    }
    
    /**
     * Source image is either the stack image being used as service image or
     * Local image build through dockerfile
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

    public boolean skipBuild(Dockerfile userDockerfile, BuildContext context) throws HyscaleException {
        if ((context.getDockerfileEntity() == null || context.getDockerfileEntity().getDockerfile() == null)
                && (userDockerfile == null)) {
            return true;
        }
        return false;
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
        if (dockerfile == null || !dockerfile.exists() || dockerfile.isDirectory()) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_BUILD);
            WorkflowLogger.endActivity(Status.FAILED);
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKERFILE_NOT_FOUND, dockerfile.getAbsolutePath());
        }
    }

}
