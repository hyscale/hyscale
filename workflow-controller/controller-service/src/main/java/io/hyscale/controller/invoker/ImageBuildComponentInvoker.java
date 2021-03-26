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
package io.hyscale.controller.invoker;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.builder.services.provider.StackImageProvider;
import io.hyscale.builder.services.service.ImageBuildPushService;
import io.hyscale.commons.component.ComponentInvoker;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DockerfileEntity;
import io.hyscale.commons.models.ImageRegistry;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.hooks.ImageCleanUpHook;
import io.hyscale.controller.manager.RegistryManager;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 *	Image builder component acts as a bridge between workflow controller and image-builder
 *	for image build and push operation provides link between
 *	{@link WorkflowContext} and {@link BuildContext}
 */
@Component
public class ImageBuildComponentInvoker extends ComponentInvoker<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(ImageBuildComponentInvoker.class);

    @Autowired
    private ImageBuildPushService imageBuildService;

    @Autowired
    private RegistryManager registryManager;

    @Autowired
    private StackImageProvider stackImageProvider;

    @Autowired
    private ImageCleanUpHook imageCleanUpHook;
    
    @PostConstruct
    public void init() {
        super.addHook(imageCleanUpHook);
    }

    @Override
    protected void doExecute(WorkflowContext context) throws HyscaleException {
        if (context == null || context.isFailed()) {
            return;
        }
        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            context.setFailed(true);
            logger.error(" Cannot build image for empty service spec");
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_REQUIRED);
        }
        String serviceName;
        try {
            serviceName = context.getServiceName() != null ? context.getServiceName()
                    : serviceSpec.get(HyscaleSpecFields.name, String.class);
        } catch (HyscaleException e) {
            logger.error("Failed to get service name, error {}", e.toString());
            throw e;
        }

        String appName = context.getAppName();
        WorkflowLogger.header(ControllerActivity.BUILD_AND_PUSH);

        // BuildContext according to imagebuilder
        BuildContext buildContext = new BuildContext();
        buildContext.setAppName(appName);
        buildContext.setServiceName(serviceName);
        buildContext.setStackAsServiceImage((Boolean) context.getAttribute(WorkflowConstants.STACK_AS_SERVICE_IMAGE));
        buildContext.setVerbose(BooleanUtils.toBoolean((Boolean) context.getAttribute(WorkflowConstants.VERBOSE)));

        List<String> registryList = getImageRegistries(serviceSpec);
        for (String registry : registryList) {
            ImageRegistry imageRegistry = registryManager.getImageRegistry(registry);
            if (imageRegistry != null) {
                buildContext.addRegistry(registry, imageRegistry);
            }
        }
        DockerfileEntity dockerfileEntity = (DockerfileEntity) context
                .getAttribute(WorkflowConstants.DOCKERFILE_ENTITY);
        buildContext.setDockerfileEntity(dockerfileEntity);

        try {
            imageBuildService.buildAndPush(serviceSpec, buildContext);
        } catch (HyscaleException e) {
            logger.error("Error while build and push for service: {}", serviceName, e);
            context.setFailed(true);
            throw e;
        } finally {
            context.addAttribute(WorkflowConstants.IMAGE_SHA_SUM, buildContext.getImageShaSum());
            context.addAttribute(WorkflowConstants.BUILD_LOGS, buildContext.getBuildLogs());
            context.addAttribute(WorkflowConstants.PUSH_LOGS, buildContext.getPushLogs());
        }
    }

    private List<String> getImageRegistries(ServiceSpec serviceSpec) throws HyscaleException {
        List<String> registriesList = new ArrayList<>();
        String pushRegistry = serviceSpec
                .get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class);
        registriesList.add(pushRegistry);
        registriesList.addAll(getPullRegistries(serviceSpec));
        return registriesList;
    }

    /**
     * Pull registries include:
     * Stack image from build spec or
     * From field in user docker file
     * @param serviceSpec
     * @return
     */
    private List<String> getPullRegistries(ServiceSpec serviceSpec) {
        List<String> pullRegistries = new ArrayList<>();
        String stackImage = stackImageProvider.getStackImageFromBuildSpec(serviceSpec);
        if (stackImage != null) {
            // buildSpec
            pullRegistries.add(stackImage.split("/")[0]);
        } else {
            // Dockerfile
            List<String> stackImages = stackImageProvider.getStackImagesFromDockerfile(serviceSpec);
            if (CollectionUtils.isNotEmpty(stackImages)) {
                Set<String> stackRegistries = stackImages.stream().filter(registry -> registry.split("/").length > 1)
                .map(registry -> registry.split("/")[0]).collect(Collectors.toSet());
                pullRegistries.addAll(stackRegistries);
            }
        }
        return pullRegistries;
    }

    @Override
    protected void onError(WorkflowContext context, HyscaleException he) throws HyscaleException {
        WorkflowLogger.header(ControllerActivity.ERROR);
        WorkflowLogger.error(ControllerActivity.CAUSE,
                he != null ? he.getMessage() : ImageBuilderErrorCodes.FAILED_TO_BUILD_AND_PUSH_IMAGE.getMessage());
        context.addAttribute(WorkflowConstants.ERROR_MESSAGE,
                (he != null) ? he.getMessage() : ImageBuilderErrorCodes.FAILED_TO_BUILD_AND_PUSH_IMAGE.getMessage());
        context.setFailed(true);
        if (he != null) {
            throw he;
        }
    }
}
