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
     * Returns all files that matches profile naming pattern in service specs directory
     * @param serviceSpecs
     * @param profileName
     * @return
     * @throws HyscaleException
     */
    public static List<File> getAllProfiles(List<File> serviceSpecs, String profileName) throws HyscaleException{
        if (serviceSpecs == null || StringUtils.isBlank(profileName)) {
            return null;
        }
        List<File> profileFiles = new ArrayList<File>();
        String profileFilePattern = getProfileNamePattern(profileName);
        for (File serviceSpec : serviceSpecs) {
            String serviceSpecPath = serviceSpec.getAbsoluteFile().getParent();
            List<File> serviceProfileFiles = HyscaleFilesUtil.listFilesWithPattern(serviceSpecPath, profileFilePattern);
            if (serviceProfileFiles != null) {
                profileFiles.addAll(serviceProfileFiles);
            }
            serviceSpecPath = serviceSpecPath.concat(ToolConstants.FILE_SEPARATOR)
                    .concat(ToolConstants.PROFILES_DIR_NAME);
            serviceProfileFiles = HyscaleFilesUtil.listFilesWithPattern(serviceSpecPath, profileFilePattern);
            if (serviceProfileFiles != null) {
                profileFiles.addAll(serviceProfileFiles);
            }
        }
        return profileFiles;
    }
    
    /**
     * 
     * @param profileName
     * @return ^(profileName-).*({@value ToolConstants#HPROF_EXTENSION})$
     */
    public static String getProfileNamePattern(String profileName) {
        if (StringUtils.isBlank(profileName)) {
            return null;
        }
        StringBuilder profilePattern = new StringBuilder();
        profilePattern.append("^(").append(profileName).append(ToolConstants.DASH).append(")").append(".*").append("(")
                .append(ToolConstants.HPROF_EXTENSION).append(")$");
        return profilePattern.toString();
    }
    
    /**
     * Every Service in service spec must have one profile
     * @param serviceSpecs
     * @param profiles
     * @return
     */
    public static List<File> validateAndFilter(List<File> serviceSpecs, List<File> profiles) throws HyscaleException {
        if (serviceSpecs == null && profiles == null) {
            return null;
        }
        if (serviceSpecs == null || serviceSpecs.isEmpty()) {
            return null;
        }
        
        if (profiles == null || profiles.isEmpty()) {
            String errorMessage = "No profiles found. Profiles are required";
            // no profiles found throw exception
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_PROCESSING_PROFILE, errorMessage );
            HyscaleException hyscaleException = new HyscaleException(ControllerErrorCodes.ERROR_WHILE_PROCESSING_PROFILE,
                    ToolConstants.INVALID_INPUT_ERROR_CODE, errorMessage);
            logger.error(hyscaleException.getMessage());
            throw hyscaleException;
        }
        
        Set<String> services = new HashSet<String>();
        Map<String, File> serviceVsProfile = new HashMap<String, File>();
        for (File serviceSpec : serviceSpecs) {
            services.add(ServiceSpecUtil.getServiceName(serviceSpec));
        }
        
        List<String> servicesWithMultipleProfile = new ArrayList<String>();
        
        for (File profile : profiles) {
            String serviceName = getServiceNameFromProfile(profile);
            if (!services.contains(serviceName)) {
                continue;
            }
            if (serviceVsProfile.get(serviceName) != null) {
                servicesWithMultipleProfile.add(serviceName);
                continue;
            }
            serviceVsProfile.put(serviceName, profile);
        }
        
        List<String> servicesWithoutProfile = services.stream().filter(each -> !serviceVsProfile.containsKey(each))
                .collect(Collectors.toList());

        if (!servicesWithoutProfile.isEmpty() || !servicesWithMultipleProfile.isEmpty()) {
            String errorMessage = getErrorMessage(servicesWithoutProfile, servicesWithMultipleProfile);
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_PROCESSING_PROFILE, errorMessage);
            HyscaleException hyscaleException = new HyscaleException(ControllerErrorCodes.ERROR_WHILE_PROCESSING_PROFILE,
                    ToolConstants.INVALID_INPUT_ERROR_CODE, errorMessage);
            logger.error(hyscaleException.getMessage());
            throw hyscaleException;
        }
        
        return serviceVsProfile.entrySet().stream().map(each -> each.getValue()).collect(Collectors.toList());
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