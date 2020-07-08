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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.commons.logger.LoggerTags;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.utils.HyscaleStringUtil;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Profile;

public class ServiceProfileUtil {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProfileUtil.class);

    /**
     * Gets service name from profile file.
     * 1.returns service name if present with the key {@link HyscaleSpecFields#overrides}.
     * 2.else returns null when file is null or throws relevant HyscaleException.
     *
     * @param profileFile
     * @return service name
     * @throws HyscaleException
     */
    public static String getServiceNameFromProfile(File profileFile) throws HyscaleException {
        return get(profileFile, HyscaleSpecFields.overrides);
    }

    /**
     * Gets profile name from profile file.
     * 1.returns profile name if present with the key @HyscaleSpecFields#environment.
     * 2.else returns null when file is null or throws relevant HyscaleException.
     *
     * @param profileFile
     * @return profile or environment name
     * @throws HyscaleException
     */
    public static String getProfileName(File profileFile) throws HyscaleException {
        return get(profileFile, HyscaleSpecFields.environment);
    }

    private static String get(File profileFile, String field) throws HyscaleException {
        if (profileFile == null) {
            return null;
        }
        try {
            Profile profile = new Profile(FileUtils.readFileToString(profileFile, ToolConstants.CHARACTER_ENCODING));
            JsonNode fieldValue = profile.get(field);
            if (fieldValue == null) {
                HyscaleException hyscaleException = new HyscaleException(
                        ServiceSpecErrorCodes.MISSING_FIELD_IN_PROFILE_FILE, field);
                logger.error(hyscaleException.getMessage());
                throw hyscaleException;
            }
            return fieldValue.asText();
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new HyscaleException(CommonErrorCode.FAILED_TO_READ_FILE, profileFile.getPath());
        }
    }
    
}