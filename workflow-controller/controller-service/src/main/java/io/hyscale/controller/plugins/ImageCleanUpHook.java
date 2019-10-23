package io.hyscale.controller.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.services.command.ImageCommandGenerator;
import io.hyscale.commons.component.InvokerHook;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.Status;
import io.hyscale.controller.core.exception.ControllerErrorCodes;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Hook to clean up local images which are no longer in use
 *
 */
@Component
public class ImageCleanUpHook implements InvokerHook<WorkflowContext> {

	private static final Logger logger = LoggerFactory.getLogger(ImageCleanUpHook.class);

	@Autowired
	private CommandExecutor commandExecutor;

	@Autowired
	private ImageCommandGenerator imageCommandGenerator;

	@Override
	public void preHook(WorkflowContext context) {

	}

	@Override
	public void postHook(WorkflowContext context) throws HyscaleException {
		ServiceSpec serviceSpec = context.getServiceSpec();
		if (serviceSpec == null) {
			logger.error(" Cannot clean up image without service specs ");
			throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_REQUIRED);
		}

		String serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);

		String tag = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.tag),
				String.class);
		String cleanUpCommand = imageCommandGenerator.getImageCleanUpCommand(context.getAppName(), serviceName, tag);
		logger.debug("Starting image cleanup, command {}", cleanUpCommand);
		boolean success = commandExecutor.execute(cleanUpCommand);

		logger.debug("Image clean up {}", success ? Status.DONE.getMessage() : Status.FAILED.getMessage());

	}

	@Override
	public void onError(WorkflowContext context, Throwable th) {
		context.setFailed(true);
	}

}
