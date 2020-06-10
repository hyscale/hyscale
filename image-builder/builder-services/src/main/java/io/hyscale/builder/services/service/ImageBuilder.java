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
package io.hyscale.builder.services.service;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.core.models.DockerImage;
import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.Image;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;

public interface ImageBuilder {

    boolean isDockerRunning();

    boolean checkForDocker();

    DockerImage _build(Dockerfile dockerfile, String tag, BuildContext context) throws HyscaleException;

    void _push(Image image, BuildContext buildContext) throws HyscaleException;

    void _pull(String image, BuildContext context) throws HyscaleException;

    void _tag(String source, Image dest) throws HyscaleException;

    /**
     * Check docker exists, pull(if required), tag, push(if required)
     *
     * @param serviceSpec
     * @param context
     * @throws HyscaleException
     */
    default void build(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        if (context == null) {
            throw new HyscaleException(ImageBuilderErrorCodes.FIELDS_MISSING, "Build Context");
        }

        WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_BUILD_STARTED);
        Dockerfile userDockerfile = serviceSpec.get(
                HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile), Dockerfile.class);
        //Skip Image Build if either of dockerfile from buildSpec or dockerfile is present
        if (skipBuild(userDockerfile, context)) {
            WorkflowLogger.endActivity(Status.SKIPPING);
        }

        // Check if docker is installed or not
        if (!checkForDocker()) {
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKER_NOT_INSTALLED);
        }

        //TEST without error message
        if (!isDockerRunning()) {
            //WorkflowLogger.error(ImageBuilderActivity.DOCKER_DAEMON_NOT_RUNNING);
            throw new HyscaleException(ImageBuilderErrorCodes.DOCKER_DAEMON_NOT_RUNNING);
        }

        String tag = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.tag),
                String.class);

        //Prepare Dockerfile for Image build
        Dockerfile dockerfile = new Dockerfile();
        dockerfile.setDockerfilePath(getDockerfilePath(userDockerfile, context));
        dockerfile.setArgs(userDockerfile != null ? userDockerfile.getArgs() : null);
        dockerfile.setTarget(userDockerfile != null ? userDockerfile.getTarget() : null);
        dockerfile.setPath(userDockerfile != null ? userDockerfile.getPath() : null);

        DockerImage dockerImage = _build(dockerfile, tag, context);
        context.setDockerImage(dockerImage);

        // validate Push
        validate(serviceSpec, context);
        String imageFullPath = ImageUtil.getImage(serviceSpec);
        String sourceImage = getSourceImageName(serviceSpec, context);

        if (context.isStackAsServiceImage()) {
            _pull(sourceImage, context);
        }
        Image image = serviceSpec.get(HyscaleSpecFields.image, Image.class);
        _tag(sourceImage, image);

        if (context.getImageRegistry() == null) {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_PUSH);
            WorkflowLogger.endActivity(Status.SKIPPING);
            return;
        }

        _push(image, context);
    }


    /**
     * Get docker file path either:
     * User docker file based on dockerfile spec or
     * Tool generated docker file
     *
     * @param userDockerfile
     * @param context
     * @return docker file path
     */
    default String getDockerfilePath(Dockerfile userDockerfile, BuildContext context) {
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
        } else {
            dockerfilePath = context.getDockerfileEntity().getDockerfile().getParent();
        }

        return dockerfilePath;
    }


    default void validate(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {
        ArrayList<String> missingFields = new ArrayList<String>();

        if (serviceSpec == null) {
            missingFields.add("ServiceSpec");
        }
        if (buildContext == null) {
            missingFields.add("BuildContext");
        }

        if (!missingFields.isEmpty()) {
            String[] missingFieldsArr = new String[missingFields.size()];
            missingFieldsArr = missingFields.toArray(missingFieldsArr);
            throw new HyscaleException(ImageBuilderErrorCodes.FIELDS_MISSING, missingFieldsArr);
        }

        String registryUrl = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class);
        if (buildContext.getImageRegistry() == null && registryUrl != null) {
            throw new HyscaleException(ImageBuilderErrorCodes.MISSING_DOCKER_REGISTRY_CREDENTIALS, registryUrl, registryUrl);
        }
    }

    default String getSourceImageName(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {

        if (buildContext.isStackAsServiceImage()) {
            return serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec,
                    HyscaleSpecFields.stackImage), String.class);
        }
        DockerImage dockerImage = buildContext.getDockerImage();

        return StringUtils.isNotBlank(dockerImage.getTag())
                ? dockerImage.getName() + ToolConstants.COLON + dockerImage.getTag()
                : dockerImage.getName();
    }

    default boolean skipBuild(Dockerfile userDockerfile, BuildContext context) throws HyscaleException {
        if ((context.getDockerfileEntity() == null || context.getDockerfileEntity().getDockerfile() == null)
                && (userDockerfile == null)) {
            return true;
        }
        return false;
    }
}
