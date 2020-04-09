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
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Profile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.utils.HyscaleStringUtil;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.commands.input.ProfileInput;
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
     *  Looks for profile files in spec as well as profiles directory.
     * @param serviceSpecs
     * @param profileName
     * @return
     * @throws HyscaleException if profile found at both the places, or no profile found
     */
    public static List<File> getProfilesFromName(List<File> serviceSpecs, String profileName) throws HyscaleException {
        if (serviceSpecs == null || StringUtils.isBlank(profileName)) {
            return null;
        }
        List<String> servicesWithoutProfile = new ArrayList<String>();
        List<String> servicesWithMultipleProfile = new ArrayList<String>();

        List<File> profileFiles = new ArrayList<File>();
        for (File serviceSpec : serviceSpecs) {
            String serviceName = ServiceSpecUtil.getServiceName(serviceSpec);
            String profileFileName = profileName + ToolConstants.DASH + serviceName + ToolConstants.HPROF_EXTENSION;
            String serviceSpecPath = serviceSpec.getAbsoluteFile().getParent();
            File profileFile = HyscaleFilesUtil.searchForFileinDir(serviceSpecPath, profileFileName);
            serviceSpecPath = serviceSpecPath.concat(ToolConstants.FILE_SEPARATOR)
                    .concat(ToolConstants.PROFILES_DIR_NAME);
            File profileFileInProfileDir = HyscaleFilesUtil.searchForFileinDir(serviceSpecPath, profileFileName);
            if (profileFile == null && profileFileInProfileDir == null) {
                // Profile not found error
                servicesWithoutProfile.add(serviceName);
                continue;
            }
            if (profileFile != null && profileFileInProfileDir != null) {
                // Multiple profile found error
                servicesWithMultipleProfile.add(serviceName);
                continue;
            }

            profileFile = profileFile == null ? profileFileInProfileDir : profileFile;
            profileFiles.add(profileFile);
        }

        if (!servicesWithoutProfile.isEmpty() || !servicesWithMultipleProfile.isEmpty()) {
            String errorMessage = getErrorMessage(servicesWithoutProfile, servicesWithMultipleProfile);
            WorkflowLogger.error(ControllerActivity.INVALID_PROFILE_NAME, profileName, errorMessage);
            HyscaleException hyscaleException = new HyscaleException(ControllerErrorCodes.INVALID_PROFILE_NAME,
                    ToolConstants.INVALID_INPUT_ERROR_CODE, profileName, errorMessage);
            logger.error("Invalid profile {}. Error {}", profileName, errorMessage);
            throw hyscaleException;
        }
        return profileFiles;
    }

    private static String getErrorMessage(List<String> servicesWithoutProfile,
            List<String> servicesWithMultipleProfile) {
        StringBuilder errMsg = new StringBuilder();

        if (!servicesWithoutProfile.isEmpty()) {
            errMsg.append("No profile file found for services ");
            servicesWithoutProfile.forEach(each -> errMsg.append(each).append(", "));
        }
        if (!servicesWithMultipleProfile.isEmpty()) {
            errMsg.append("Multiple profile files found for services ");
            servicesWithMultipleProfile.forEach(each -> errMsg.append(each).append(", "));
        }
        return HyscaleStringUtil.removeSuffixStr(errMsg, ", ");
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
        return get(profileFile, HyscaleSpecFields.overrides);
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

    public static boolean isProfileNameGiven(ProfileInput profileInput) {
        if (profileInput != null && StringUtils.isNotBlank(profileInput.getProfileName())) {
            return true;
        }

        return false;
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