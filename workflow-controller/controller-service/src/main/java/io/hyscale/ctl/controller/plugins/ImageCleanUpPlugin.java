package io.hyscale.ctl.controller.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.builder.services.command.ImageCommandGenerator;
import io.hyscale.ctl.commons.component.ComponentInvokerPlugin;
import io.hyscale.ctl.commons.commands.CommandExecutor;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.Status;
import io.hyscale.ctl.controller.core.exception.ControllerErrorCodes;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

@Component
public class ImageCleanUpPlugin implements ComponentInvokerPlugin<WorkflowContext> {

	private static final Logger logger = LoggerFactory.getLogger(ImageCleanUpPlugin.class);

	@Autowired
	private CommandExecutor commandExecutor;

	@Autowired
	private ImageCommandGenerator imageCommandGenerator;

	@Override
	public void doBefore(WorkflowContext context) {

	}

	@Override
	public void doAfter(WorkflowContext context) throws HyscaleException {
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
