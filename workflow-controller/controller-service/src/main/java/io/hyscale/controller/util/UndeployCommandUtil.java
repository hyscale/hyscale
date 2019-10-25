package io.hyscale.controller.util;

import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;

public class UndeployCommandUtil {

	public static void logWorkflowInfo() {

		WorkflowLogger.header(ControllerActivity.UNDEPLOYMENT_INFO);

		WorkflowLogger.logPersistedActivities();

		WorkflowLogger.info(ControllerActivity.UNDEPLOYMENT_DONE);
	}
}
