package io.hyscale.ctl.controller.util;

import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.constants.WorkflowConstants;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class CommandUtil {

	public static String getEnvName(String profile, String appName) {
		if (StringUtils.isNotBlank(profile)) {
			return FilenameUtils.getBaseName(profile);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(appName).append(WorkflowConstants.DASH).append(WorkflowConstants.DEV_ENV);
		return sb.toString();
	}

	public static void logMetaInfo(String info, ControllerActivity controllerActivity) {
		if (StringUtils.isNotBlank(info)) {
			WorkflowLogger.info(controllerActivity, info);
		}
	}

}
