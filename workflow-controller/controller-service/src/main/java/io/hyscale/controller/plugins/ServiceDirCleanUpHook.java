package io.hyscale.controller.plugins;

import io.hyscale.commons.component.InvokerHook;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.HyscaleFilesUtil;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Hook to clean service directory to remove files no longer required
 *
 */
@Component
public class ServiceDirCleanUpHook implements InvokerHook<WorkflowContext> {

	private static final Logger logger = LoggerFactory.getLogger(ServiceDirCleanUpHook.class);

	@Autowired
	private SetupConfig setupConfig;

	@Autowired
	private HyscaleFilesUtil filesUtil;

	@Override
	public void preHook(WorkflowContext context) throws HyscaleException {
		if (context.getServiceName() != null && context.getAttribute(WorkflowConstants.CLEAN_UP_SERVICE_DIR) != null
				&& context.getAttribute(WorkflowConstants.CLEAN_UP_SERVICE_DIR).equals(true)) {
			String serviceDir = setupConfig.getServiceDir(context.getAppName(), context.getServiceName());
			filesUtil.deleteDirectory(serviceDir);
			logger.debug("Cleaning up service dir in the apps");
		}
	}

	@Override
	public void postHook(WorkflowContext context) throws HyscaleException {

	}

	@Override
	public void onError(WorkflowContext context, Throwable th) {

	}
}
