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
package io.hyscale.controller.converters;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.HyscaleSpecType;
import io.hyscale.controller.util.ServiceProfileUtil;
import io.hyscale.servicespec.commons.activity.ServiceSpecActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;

import java.io.File;

/**
Provides parameters and funtions such as profile reference schema,Regex for profile file naming,
data validation and respective error messages  for profile file validation.
 */
public class ProfileConverter extends Converter {
    @Autowired
    BuildProperties buildProperties;

    private static final Logger logger = LoggerFactory.getLogger(ProfileConverter.class);

    @Override
    public String getFilePattern() {
        return ValidationConstants.PROFILE_FILENAME_REGEX;
    }


    @Override
    public HyscaleSpecType getReferenceSchemaType() {
        return HyscaleSpecType.PROFILE;
    }

    @Override
    public ServiceSpecActivity getWarnMessage() {
        return ServiceSpecActivity.IMPROPER_PROFILE_FILE_NAME;
    }

    @Override
    public boolean validateData(File profileFile) throws HyscaleException {
        String profileFileName = profileFile.getName().split("\\.")[0];
        int dashIndex = profileFileName.indexOf(ToolConstants.DASH);
        if (dashIndex < 0) {
            WorkflowLogger.warn(ServiceSpecActivity.PROFILE_NAME_MISMATCH, profileFile.getName());
            logger.warn(ServiceSpecActivity.PROFILE_NAME_MISMATCH.getActivityMessage(), profileFileName);
            return false;
        }
        StringBuilder profileNameBuilder = new StringBuilder();
        profileNameBuilder.append(ServiceProfileUtil.getProfileName(profileFile)).append(ToolConstants.DASH).append(ServiceProfileUtil.getServiceNameFromProfile(profileFile));
        if (!profileFileName.equals(profileNameBuilder.toString())) {
            logger.warn(ServiceSpecActivity.PROFILE_NAME_MISMATCH.getActivityMessage(), profileFile.getName());
            WorkflowLogger.persist(ServiceSpecActivity.PROFILE_NAME_MISMATCH, profileFile.getName());
            return false;
        }
        return true;
    }
}
