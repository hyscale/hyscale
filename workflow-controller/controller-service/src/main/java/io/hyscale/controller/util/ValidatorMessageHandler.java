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

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.StructuredOutputHandler;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.exception.ControllerErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorMessageHandler.class);

    public static void handleErrMsg(StringBuilder exceptionMsg, boolean isInvalid, boolean isFailed)
            throws HyscaleException {
        handleErrMsg(exceptionMsg.toString(), isInvalid, isFailed);
    }

    public static void handleErrMsg(String exceptionMsg, boolean isInvalid, boolean isFailed) throws HyscaleException {
        exceptionMsg = exceptionMsg.trim();
        if (isInvalid || isFailed) {
            logger.error("Input invalid : {}, failed: {}, error message : {}", isInvalid, isFailed, exceptionMsg);

        }
        WorkflowLogger.logPersistedActivities();
        if (isFailed) {
            if (WorkflowLogger.isDisabled() && exceptionMsg != null && !exceptionMsg.isEmpty()) {
                exceptionMsg = exceptionMsg.startsWith(": \n") ? exceptionMsg.substring(3) : exceptionMsg;
                StructuredOutputHandler.prepareOutput(WorkflowConstants.DEPLOYMENT_ERROR, exceptionMsg);
            }
            throw new HyscaleException(ControllerErrorCodes.INPUT_VALIDATION_FAILED,
                    ToolConstants.INVALID_INPUT_ERROR_CODE, exceptionMsg);
        }
    }
}
