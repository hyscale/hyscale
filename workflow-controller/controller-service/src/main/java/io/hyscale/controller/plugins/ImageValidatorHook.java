package io.hyscale.controller.plugins;

import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.commons.component.InvokerHook;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.core.exception.ControllerErrorCodes;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Hook to validate image details in service spec
 *
 */
@Component
public class ImageValidatorHook implements InvokerHook<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(ImageValidatorHook.class);

    @Override
    public void preHook(WorkflowContext context) throws HyscaleException {
        logger.debug("Executing {}", getClass());
        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_REQUIRED);
        }
        String imageName = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.name), String.class);
        if (StringUtils.isBlank(imageName)) {
            throw new HyscaleException(ImageBuilderErrorCodes.CANNOT_RESOLVE_IMAGE_NAME);
        }
    }

    @Override
    public void postHook(WorkflowContext context) throws HyscaleException {

    }

    @Override
    public void onError(WorkflowContext context, Throwable th) {
        context.setFailed(true);
    }
}
