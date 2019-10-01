package io.hyscale.ctl.controller.invoker;

import javax.annotation.PostConstruct;

import io.hyscale.ctl.builder.services.exception.ImageBuilderErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.builder.core.models.BuildContext;
import io.hyscale.ctl.commons.component.ComponentInvoker;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.DockerfileEntity;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.constants.WorkflowConstants;
import io.hyscale.ctl.controller.core.exception.ControllerErrorCodes;
import io.hyscale.ctl.controller.manager.RegistryManager;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.controller.plugins.ImageCleanUpPlugin;
import io.hyscale.ctl.builder.services.service.ImageBuildPushService;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

@Component
public class ImageBuildComponentInvoker extends ComponentInvoker<WorkflowContext> {

    @Autowired
    private ImageBuildPushService imageBuildService;

    @Autowired
    private RegistryManager registryManager;

    @Autowired
    private ImageCleanUpPlugin imageCleanUpPlugin;

    @PostConstruct
    public void init() {
        super.addPlugin(imageCleanUpPlugin);
    }

    private static final Logger logger = LoggerFactory.getLogger(ImageBuildComponentInvoker.class);

    @Override
    protected void doExecute(WorkflowContext context) throws HyscaleException {
        if (context == null || context.isFailed()) {
            return;
        }
        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            context.setFailed(true);
            logger.error(" Cannot generate manifests with empty service specs ");
            throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_REQUIRED);
        }
        String serviceName;
        try {
            serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
        } catch (HyscaleException e) {
            logger.error("Failed to get service name, error {}", e.toString());
            return;
        }

        String appName = context.getAppName();
        WorkflowLogger.header(ControllerActivity.BUILD_AND_PUSH);

        // BuildContext according to imagebuilder
        BuildContext buildContext = new BuildContext();
        buildContext.setAppName(appName);
        buildContext.setServiceName(serviceName);
        Boolean stackAsServiceImage = (Boolean) context.getAttribute(WorkflowConstants.STACK_AS_SERVICE_IMAGE);
        buildContext.setStackAsServiceImage(stackAsServiceImage == null ? false : stackAsServiceImage);
        buildContext.setVerbose((Boolean) context.getAttribute(WorkflowConstants.VERBOSE));
        buildContext.setImageRegistry(registryManager.getImageRegistry(serviceSpec
                .get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class)));

        DockerfileEntity dockerfileEntity = (DockerfileEntity) context
                .getAttribute(WorkflowConstants.DOCKERFILE_ENTITY);
        buildContext.setDockerfileEntity(dockerfileEntity);

        try {
            imageBuildService.buildAndPush(serviceSpec, buildContext);
        } catch (HyscaleException e) {
            context.setFailed(true);
            throw e;
        } finally {
            context.addAttribute(WorkflowConstants.IMAGE_SHA_SUM,
                    buildContext.getImageShaSum());
            context.addAttribute(WorkflowConstants.BUILD_LOGS,
                    buildContext.getBuildLogs());
            context.addAttribute(WorkflowConstants.PUSH_LOGS,
                    buildContext.getPushLogs());
        }

    }

    @Override
    protected void onError(WorkflowContext context, HyscaleException he) {
        WorkflowLogger.header(ControllerActivity.ERROR);
        WorkflowLogger.error(ControllerActivity.CAUSE, he != null ? he.getMessage() : ImageBuilderErrorCodes.FAILED_TO_BUILD_AND_PUSH_IMAGE.getErrorMessage());
        context.setFailed(true);
    }
}
