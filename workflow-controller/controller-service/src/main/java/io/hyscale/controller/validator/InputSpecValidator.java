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

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.logger.LoggerTags;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.activity.ControllerActivity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Activity;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.util.ValidatorMessageHandler;

/**
 * Ensures validators are called for all the input files even if some fails validation
 *
 * @author tushar
 */
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
        StringBuilder exceptionMsgBuilder = new StringBuilder().append(": \n");
        for (File inputSpecFile : inputSpecFiles) {
            try {
                if (!validateFile(inputSpecFile) || !getSchemaValidator().validate(inputSpecFile)) {
                    isInvalid = true;
                }
            } catch (HyscaleException e) {
                isFailed = true;
                exceptionMsgBuilder.append(e.getMessage()).append(ToolConstants.NEW_LINE);
            }
        }
        ValidatorMessageHandler.handleErrMsg(exceptionMsgBuilder, isInvalid, isFailed);
        return !isInvalid;
    }
    
    private boolean validateFile(File inputFile) throws HyscaleException {
        logger.debug("Running Validator {}", getClass());
        if (inputFile == null) {
            return false;
        }
        String inputFilePath = inputFile.getAbsolutePath();

        if (WindowsUtil.isHostWindows()) {
            inputFilePath = WindowsUtil.updateToUnixFileSeparator(inputFilePath);
        }

        if (StringUtils.isBlank(inputFilePath)) {
            WorkflowLogger.persist(ControllerActivity.EMPTY_FILE_PATH, LoggerTags.ERROR);
            throw new HyscaleException(CommonErrorCode.EMPTY_FILE_PATH,
                    ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
        }

        if (!inputFile.exists()) {
            WorkflowLogger.persist(ControllerActivity.CANNOT_FIND_FILE, LoggerTags.ERROR, inputFilePath);
            throw new HyscaleException(CommonErrorCode.FILE_NOT_FOUND,
                    ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE, inputFilePath);
        }
        if (inputFile.isDirectory()) {
            WorkflowLogger.persist(ControllerActivity.DIRECTORY_INPUT_FOUND, LoggerTags.ERROR, inputFilePath);
            throw new HyscaleException(CommonErrorCode.FOUND_DIRECTORY_INSTEAD_OF_FILE,
                    ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE, inputFilePath);
        }
        if (!inputFile.isFile()) {
            WorkflowLogger.persist(ControllerActivity.INVALID_FILE_INPUT, LoggerTags.ERROR, inputFilePath);
            throw new HyscaleException(CommonErrorCode.INVALID_FILE_INPUT,
                    ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE, inputFilePath);
        }
        String fileName = inputFile.getName();
        if (!fileName.matches(getFilePattern())) {
            WorkflowLogger.persist(getWarnMessage(), fileName);
            logger.warn(getWarnMessage().getActivityMessage(), fileName);
        }
        if (inputFile.length() == 0) {
            WorkflowLogger.persist(ControllerActivity.EMPTY_FILE_FOUND, LoggerTags.ERROR, inputFilePath);
            throw new HyscaleException(CommonErrorCode.EMPTY_FILE_FOUND,
                    ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE, inputFilePath);
        }

        return true;
    }

    protected abstract Validator<File> getSchemaValidator();

    protected abstract Activity getWarnMessage();

    protected abstract String getFilePattern();

}
