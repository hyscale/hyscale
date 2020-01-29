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
package Converters;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.activity.ServiceSpecActivity;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Profile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/*
Provides parameters and funtions such as profile reference schema,Regex for profile file naming,
data validation and respective warning and error messages  for profile file validation.
 */
public class ProfileConverter extends Converter {
    private static final Logger logger = LoggerFactory.getLogger(ProfileConverter.class);

    private static final String PROFILE_SCHEMA_FILE = "/hprof/" + ToolConstants.RELEASE + "/profile-spec.json";

    @Override
    public String getFilePattern() {
        return ValidationConstants.PROFILE_FILENAME_REGEX;
    }


    @Override
    public String getReferenceSchema() {
        return PROFILE_SCHEMA_FILE;
    }

    @Override
    public String getWarnMessage() {
        return ValidationConstants.INVALID_PROFILE_FILE_NAME_MSG;
    }

    @Override
    public void validateData(File profileFile) throws HyscaleException {
        String profileFileName = FilenameUtils.getBaseName(profileFile.getName());
        int dashIndex = profileFileName.indexOf(ToolConstants.DASH);
        if (dashIndex < 0) {
            logger.warn(ServiceSpecActivity.PROFILE_NAME_MISMATCH.getActivityMessage(), profileFileName);
        }else {
            try {
                String serviceName = profileFileName.substring(0, profileFileName.indexOf(ToolConstants.DASH));
                String envName = profileFileName.substring(profileFileName.indexOf(ToolConstants.DASH) + 1);
                Profile profile = new Profile(FileUtils.readFileToString(profileFile, ToolConstants.CHARACTER_ENCODING));
                if (!serviceName.equals(profile.get(HyscaleSpecFields.overrides).asText()) || !envName.equals(profile.get(HyscaleSpecFields.environment).asText())) {
                    logger.warn(ServiceSpecActivity.PROFILE_NAME_MISMATCH.getActivityMessage(), profileFileName);
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
                throw new HyscaleException(CommonErrorCode.FAILED_TO_READ_FILE, profileFileName);
            }
        }
    }
}
