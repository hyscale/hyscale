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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Activity;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.activity.ControllerActivity;

public abstract class FileValidator implements Validator<File> {

    private static final Logger logger = LoggerFactory.getLogger(FileValidator.class);

    @Override
    public boolean validate(File inputFile) throws HyscaleException {
        logger.debug("Running Validator {}", getClass());
        if (inputFile == null) {
            return false;
        }
        // TODO is this required ?
        //        if (WindowsUtil.isHostWindows()) {
        //            inputFilePath = WindowsUtil.updateToUnixFileSeparator(inputFilePath);
        //        }

        String inputFilePath = inputFile.getAbsolutePath();
        if (StringUtils.isBlank(inputFilePath)) {
            WorkflowLogger.error(ControllerActivity.EMPTY_FILE_PATH);
            throw new HyscaleException(CommonErrorCode.EMPTY_FILE_PATH,
                    ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
        }

        if (!inputFile.exists()) {
            WorkflowLogger.persistError(ControllerActivity.CANNOT_FIND_FILE, inputFilePath);
            throw new HyscaleException(CommonErrorCode.FILE_NOT_FOUND,
                    ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE, inputFilePath);
        }
        if (inputFile.isDirectory()) {
            WorkflowLogger.persistError(ControllerActivity.DIRECTORY_INPUT_FOUND, inputFilePath);
            throw new HyscaleException(CommonErrorCode.FOUND_DIRECTORY_INSTEAD_OF_FILE,
                    ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE, inputFilePath);
        }
        if (!inputFile.isFile()) {
            WorkflowLogger.persistError(ControllerActivity.INVALID_FILE_INPUT, inputFilePath);
            throw new HyscaleException(CommonErrorCode.INVALID_FILE_INPUT,
                    ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE, inputFilePath);
        }
        String fileName = inputFile.getName();
        if (!fileName.matches(getFilePattern())) {
            WorkflowLogger.persist(getWarnMessage(), fileName);
            logger.warn(getWarnMessage().getActivityMessage(), fileName);
        }
        if (inputFile.length() == 0) {
            WorkflowLogger.persistError(ControllerActivity.EMPTY_FILE_FOUND, inputFilePath);
            throw new HyscaleException(CommonErrorCode.EMPTY_FILE_FOUND,
                    ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE, inputFilePath);
        }

        return true;
    }

    protected abstract Activity getWarnMessage();

    protected abstract String getFilePattern();
}
