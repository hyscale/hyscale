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

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.model.HyscaleCommandSpec;
import io.hyscale.controller.util.ServiceProfileUtil;

@Component
public class InputSpecValidator implements Validator<HyscaleCommandSpec> {
    
    private static final Logger logger = LoggerFactory.getLogger(InputSpecValidator.class);

    @Autowired
    private ServiceSpecFileValidator serviceSpecFileValidator;

    @Autowired
    private ServiceSpecSchemaValidator serviceSpecSchemaValidator;

    @Autowired
    private ProfileFileValidator profileFileValidator;

    @Autowired
    private ProfileSpecSchemaValidator profileSpecSchemaValidator;

    /**
     * Steps:
     * FileValidator for service spec
     * SchemaValidator - Service Spec
     * 
     * 1. If profile Name given:
     *      Process - getProfileFiles - return List<ProfileFile> throws exception
     *          ServiceSpec File to Model
     *          Get All profile Files - 
     *      
     * FileValidator for profiles    
     * SchemaValidator Profiles
     */
    @Override
    public boolean validate(HyscaleCommandSpec commandSpec) throws HyscaleException {
        if (commandSpec == null) {
            return false;
        }
        List<File> serviceSpecs = commandSpec.getServiceSpecFiles();

        if (isInvalid(serviceSpecFileValidator, serviceSpecs)) {
            return false;
        }

        if (isInvalid(serviceSpecSchemaValidator, serviceSpecs)) {
            return false;
        }

        List<File> profiles = commandSpec.getProfileFiles();
        if (profiles == null && commandSpec.getProfileName() == null) {
            return true;
        }

        if (profiles == null && commandSpec.getProfileName() != null) {
            profiles = ServiceProfileUtil.getProfilesFromName(serviceSpecs, commandSpec.getProfileName());
            commandSpec.setProfileFiles(profiles);
        }

        if (commandSpec.getProfileName() == null && isInvalid(profileFileValidator, profiles)) {
            return false;
        }

        if (isInvalid(profileSpecSchemaValidator, profiles)) {
            return false;
        }

        return true;
    }

    private boolean isInvalid(Validator<File> validator, List<File> inputList) throws HyscaleException {
        logger.debug("Running validator: {}", validator.getClass());
        boolean isInvalid = false;
        boolean isFailed = false;
        StringBuilder exceptionMsg = new StringBuilder();
        for (File serviceSpecFile : inputList) {
            try {
                isInvalid = validator.validate(serviceSpecFile) ? isInvalid : true;
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
        return isInvalid;
    }

}
