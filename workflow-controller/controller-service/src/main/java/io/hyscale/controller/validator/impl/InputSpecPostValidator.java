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
package io.hyscale.controller.validator.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.model.WorkflowContext;

@Component
public class InputSpecPostValidator implements Validator<List<WorkflowContext>> {

    private static final Logger logger = LoggerFactory.getLogger(InputSpecPostValidator.class);

    private List<Validator<WorkflowContext>> validators = new ArrayList<Validator<WorkflowContext>>();

    public void addValidator(Validator<WorkflowContext> validator) {
        if (validator != null) {
            validators.add(validator);
        }
    }

    @Override
    public boolean validate(List<WorkflowContext> contextList) throws HyscaleException {
        boolean isInvalid = false;
        boolean isFailed = false;
        StringBuilder exceptionMsg = new StringBuilder();
        for (Validator<WorkflowContext> validator : validators) {
            logger.debug("Running validator: {}", validator.getClass());
            if (contextList == null) {
                try {
                    isInvalid = validator.validate(null) ? isInvalid : true;
                } catch (HyscaleException e) {
                    isFailed = true;
                    exceptionMsg.append(e.getMessage()).append("\n");
                }
            } else {
                for (WorkflowContext context : contextList) {
                    try {
                        isInvalid = validator.validate(context) ? isInvalid : true;
                    } catch (HyscaleException e) {
                        isFailed = true;
                        exceptionMsg.append(e.getMessage()).append("\n");
                    }
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
            if (isInvalid) {
                return !isInvalid;
            }
        }
        return !isInvalid;
    }

}
