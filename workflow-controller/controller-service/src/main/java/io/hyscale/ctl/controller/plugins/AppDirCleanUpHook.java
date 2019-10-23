package io.hyscale.ctl.controller.plugins;

import io.hyscale.ctl.commons.component.InvokerHook;
import io.hyscale.ctl.commons.config.SetupConfig;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.utils.HyscaleFilesUtil;
import io.hyscale.ctl.controller.constants.WorkflowConstants;
import io.hyscale.ctl.controller.model.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Plugin to clean apps directory to remove files no longer required
 *
 */
@Component
public class AppDirCleanUpHook implements InvokerHook<WorkflowContext> {
	private static final Logger logger = LoggerFactory.getLogger(ServiceDirCleanUpHook.class);

	@Autowired
	private SetupConfig setupConfig;

	@Autowired
	private HyscaleFilesUtil filesUtil;

	@Override
	public void preHook(WorkflowContext context) throws HyscaleException {
		if (context.getAppName() != null && context.getAttribute(WorkflowConstants.CLEAN_UP_APP_DIR) != null
				&& context.getAttribute(WorkflowConstants.CLEAN_UP_APP_DIR).equals(true)) {
			String appDir = setupConfig.getAppsDir() + context.getAppName();
			filesUtil.deleteDirectory(appDir);
			logger.debug("Cleaning up app dir in the apps");
		}
	}

	@Override
	public void postHook(WorkflowContext context) throws HyscaleException {

	}

	@Override
	public void onError(WorkflowContext context, Throwable th) {

	}
}
