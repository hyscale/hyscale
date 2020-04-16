/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.controller.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.constants.WorkflowConstants;

/**
 * Utility for commands
 *
 */
public class CommandUtil {
    
	public static String getEnvName(String profile){
		if (StringUtils.isNotBlank(profile)) {
			return profile;
		}
		return WorkflowConstants.DEV_ENV;
	}

	public static void logMetaInfo(String info, ControllerActivity controllerActivity) {
		if (StringUtils.isBlank(info)) {
			return;
		}
		info = WindowsUtil.updateToHostFileSeparator(info);
		WorkflowLogger.info(controllerActivity, info);
	}
	
	/**
	 * Checks if input is valid based on Bean Validation
	 * @param object
	 * @return true if input valid else false
	 */
	public static boolean isInputValid(Object object) {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(object);
        List<String> errorMsgs = new ArrayList<String>();
        if (violations != null && !violations.isEmpty()) {
            for (ConstraintViolation<Object> violation : violations) {
                String errMsg = violation.getMessage().replaceAll("\\{\\}", violation.getInvalidValue().toString());
            	errorMsgs.add(errMsg);
            }
            errorMsgs.forEach( each -> {
                WorkflowLogger.error(ControllerActivity.INVALID_INPUT, each);
            });
            return false;
        }
        
        return true;
	}
	
}