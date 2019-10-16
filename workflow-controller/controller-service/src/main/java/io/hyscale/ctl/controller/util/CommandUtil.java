package io.hyscale.ctl.controller.util;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.constants.WorkflowConstants;

/**
 * Utility for commands
 *
 */
public class CommandUtil {

	/**
	 * gets environment name for labels in resources
	 * @param profile
	 * @param appName
	 * @return environment name
	 */
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
	
	/**
	 * Checks if input is valid based on BeanValidation
	 * @param object
	 * @return true if input valid else false
	 */
	public static boolean isInputValid(Object object) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
        	StringBuilder errorMsgBuilder = new StringBuilder();
            for (ConstraintViolation<Object> violation : violations) {
            	errorMsgBuilder.append(violation.getMessage() + "\n");
            }
            WorkflowLogger.error(ControllerActivity.INVALID_INPUT, errorMsgBuilder.toString());
            return false;
        }
        
        return true;
	}

}
