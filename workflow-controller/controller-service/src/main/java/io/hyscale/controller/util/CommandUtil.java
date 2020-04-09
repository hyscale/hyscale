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
import java.util.Map;
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
import io.hyscale.controller.model.EffectiveServiceSpec;
import io.hyscale.controller.model.WorkflowContext;
import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.IHelpSectionRenderer;

/**
 * Utility for commands
 *
 */
public class CommandUtil {
    
    public static final String PROFILE_DIR_OPTION = "-P";
    
    public static final String TEMP_PROFILE_DIR_OPTION = "-z";
    
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
	
	/**
     * TODO remove this once Picocli supports case sensitive options
     * @see https://github.com/remkop/picocli/issues/154
     * @param args
     * @return
     */
    public static String[] updateArgs(String[] args) {
        if (args == null) {
            return args;
        }
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i].equals(PROFILE_DIR_OPTION)) {
                args[i] = TEMP_PROFILE_DIR_OPTION;
            }
        }
        return args;
    }

    /**
     * TODO remove once Picocli supports case sensitive options
     * Unless help modification is required
     * @see https://github.com/remkop/picocli/issues/154
     * @param commandLine
     * @return map of help message
     */
    public static Map<String, IHelpSectionRenderer> updateHelp(CommandLine commandLine) {
        Map<String, IHelpSectionRenderer> helpSectionMap = commandLine.getHelpSectionMap();
        helpSectionMap.put(CommandLine.Model.UsageMessageSpec.SECTION_KEY_SYNOPSIS, new IHelpSectionRenderer() {
            @Override
            public String render(Help help) {
                String message = help.synopsis(help.synopsisHeadingLength());
                return message != null ? message.replaceAll(TEMP_PROFILE_DIR_OPTION, PROFILE_DIR_OPTION) : message;
            }
        });
        helpSectionMap.put(CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST, new IHelpSectionRenderer() {
            @Override
            public String render(Help help) {
                String message = help.optionList();
                return message != null ? message.replaceAll(TEMP_PROFILE_DIR_OPTION, PROFILE_DIR_OPTION) : message;
            }
        });
        return helpSectionMap;
    }
    
    public static String updateMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return message;
        }
        return message.replaceAll(TEMP_PROFILE_DIR_OPTION, PROFILE_DIR_OPTION);
    }
    
}