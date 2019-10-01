package io.hyscale.ctl.builder.services.impl;

import io.hyscale.ctl.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.ctl.builder.services.service.ImageBuildPushService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.builder.core.models.BuildContext;
import io.hyscale.ctl.builder.core.models.ImageBuilderActivity;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.servicespec.commons.model.service.Dockerfile;
import io.hyscale.ctl.commons.models.Status;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

@Component
public class LocalImageBuildPushServiceImpl implements ImageBuildPushService {

    private static final Logger logger = LoggerFactory.getLogger(LocalImageBuildPushServiceImpl.class);

    @Autowired
    private LocalImageBuildServiceImpl buildService;

    @Autowired
    private LocalImagePushServiceImpl pushService;

    @Override
    public void buildAndPush(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {

        if (validate(serviceSpec) && isImageBuildPushRequired(serviceSpec, context)) {
            context = buildService.build(serviceSpec, context);
            pushService.pushImage(serviceSpec, context);
        } else {
            WorkflowLogger.startActivity(ImageBuilderActivity.IMAGE_BUILD_PUSH);
            WorkflowLogger.endActivity(Status.SKIPPING);
        }
    }

    /**
     * Not required if dockerSpec and dockerfile not available In case its just a
     * stack image, need to push only
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

    private boolean validate(ServiceSpec serviceSpec) throws HyscaleException {
        String imageName = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.name), String.class);
        if (StringUtils.isBlank(imageName)) {
            throw new HyscaleException(ImageBuilderErrorCodes.CANNOT_RESOLVE_IMAGE_NAME);
        }
        return true;
    }

}
