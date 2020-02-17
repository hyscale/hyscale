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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Profile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.exception.ControllerErrorCodes;

public class ServiceProfileUtil {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProfileUtil.class);

    public static Map<String, File> getServiceProfileMap(List<File> profiles) throws HyscaleException {
        Map<String, File> serviceProfileMap = new HashMap<String, File>();
        if (profiles == null) {
            return serviceProfileMap;
        }
        for (File profile : profiles) {
            String serviceName = getServiceNameFromProfile(profile);
            if (serviceProfileMap.get(serviceName) == null) {
                serviceProfileMap.put(serviceName, profile);
            } else {
                // multiple profile for single service - fail
                HyscaleException hyscaleException = new HyscaleException(ControllerErrorCodes.UNIQUE_PROFILE_REQUIRED,
                        serviceName);
                logger.error("Service: {}, have more than 1 profile", serviceName, hyscaleException);
                throw hyscaleException;
            }
        }
        return serviceProfileMap;
    }

    /**
     * Gets service name from profile file.
     * 1.returns service name if present with the key {@link HyscaleSpecFields#overrides}.
     * 2.else returns null when file is null or throws relative HyscaleException.
     *
     * @param profileFile
     * @return service name
     * @throws HyscaleException
     */
    public static String getServiceNameFromProfile(File profileFile) throws HyscaleException {
       return get(profileFile,HyscaleSpecFields.overrides);
    }

    /**
     * Gets profile name from profile file.
     * 1.returns profile name if present with the key @HyscaleSpecFields#environment.
     * 2.else returns null when file is null or throws relative HyscaleException.
     *
     * @param profileFile
     * @return profile or environment name
     * @throws HyscaleException
     */
    public static String getProfileName(File profileFile) throws HyscaleException{
       return get(profileFile,HyscaleSpecFields.environment);
    }

    private static String get(File profileFile, String field) throws HyscaleException{
        if (profileFile == null) {
            return null;
        }
        try {
            Profile profile = new Profile(FileUtils.readFileToString(profileFile, ToolConstants.CHARACTER_ENCODING));
            JsonNode fieldValue = profile.get(field);
            if (fieldValue == null) {
                HyscaleException hyscaleException = new HyscaleException(ServiceSpecErrorCodes.MISSING_FIELD_IN_PROFILE_FILE, field);
                logger.error(hyscaleException.getMessage());
                throw hyscaleException;
            }
            return fieldValue.asText();
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new HyscaleException(CommonErrorCode.FAILED_TO_READ_FILE, profileFile.getPath());
        }
    }

    
    public static void printWarnMsg(Map<String, File> serviceProfileMap) {
        if (serviceProfileMap == null || serviceProfileMap.isEmpty()) {
            return;
        }
        String services = serviceProfileMap.keySet().toString();
        WorkflowLogger.footer();
        WorkflowLogger.warn(ControllerActivity.NO_SERVICE_FOUND_FOR_PROFILE, services);
        WorkflowLogger.footer();
    }
}
