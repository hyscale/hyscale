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
package io.hyscale.controller.validator;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.exception.ControllerErrorCodes;

public abstract class InputSpecValidator implements Validator<List<File>> {

    private static final Logger logger = LoggerFactory.getLogger(InputSpecValidator.class);

    @Override
    public boolean validate(List<File> inputSpecFiles) throws HyscaleException {
        logger.debug("Running validator: {}", this.getClass());
        if (inputSpecFiles == null) {
            return false;
        }

        boolean isInvalid = false;
        boolean isFailed = false;
        StringBuilder exceptionMsg = new StringBuilder();
        for (File inputSpecFile : inputSpecFiles) {
            try {
                if (!getFileValidator().validate(inputSpecFile) || !getSchemaValidator().validate(inputSpecFile)) {
                    isInvalid = true;
                }
            } catch (HyscaleException e) {
                isFailed = true;
                exceptionMsg.append(e.getMessage()).append("\n");
            }
        }
        if (isInvalid || isFailed) {
            logger.error("Input invalid : {}, failed: {}, error message : {}", isInvalid, isFailed,
                    exceptionMsg.toString());
            WorkflowLogger.logPersistedActivities();

        }
        if (isFailed) {
            throw new HyscaleException(ControllerErrorCodes.INPUT_VALIDATION_FAILED,
                    ToolConstants.INVALID_INPUT_ERROR_CODE, exceptionMsg.toString());
        }
        return !isInvalid;
    }

    protected abstract Validator<File> getFileValidator();

    protected abstract Validator<File> getSchemaValidator();

}
