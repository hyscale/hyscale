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
package io.hyscale.controller.exception;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.PrintWriter;

/**
 * Exception Handler for CommandLine Parameter Exception Handling.
 * Catches exception thrown IParameterExceptionHandler,
 * 1.if cause is HyscaleException and returns hyscale error code.
 * 2.else prints respective command usage.
 */
@Component
public class ParameterExceptionHandler implements CommandLine.IParameterExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ParameterExceptionHandler.class);

    @Override
    public int handleParseException(CommandLine.ParameterException ex, String[] args) {

        if (ex == null) {
            return ToolConstants.HYSCALE_SUCCESS_CODE;
        }
        if (ex.getCause() instanceof HyscaleException) {
            logger.error(ex.getMessage());
            return ToolConstants.HYSCALE_ERROR_CODE;
        }
        CommandLine commandLine = ex.getCommandLine();
        PrintWriter writer = commandLine.getErr();
        writer.printf(commandLine.getUsageMessage());
        logger.error(ex.getMessage());
        return ToolConstants.INVALID_INPUT_ERROR_CODE;
    }
}
