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
package io.hyscale.controller.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.activity.ValidatorActivity;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.model.HyscaleCommandSpecBuilder;
import io.hyscale.controller.model.HyscaleInputSpec;
import io.hyscale.controller.util.ServiceProfileUtil;
import io.hyscale.controller.util.ServiceSpecUtil;
import io.hyscale.controller.validator.impl.ProfileSpecInputValidator;
import io.hyscale.controller.validator.impl.ServiceSpecInputValidator;

/**
 * Provides input processing funtionality
 * like processing service spec, profiles
 * Processing includes validating input,
 * looking for profiles among others
 * 
 * @author tushar
 *
 */
@Component
public class HyscaleInputSpecProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(HyscaleInputSpecProcessor.class);

    /**
     * This class extracts out common functionality between manifest generation and deployer
     * Especially related to service and profile spec validation and processing
     * 
     */

    @Autowired
    private ServiceSpecInputValidator serviceSpecInputValidator;
    
    @Autowired
    private ProfileSpecInputValidator profileSpecInputValidator;
    
    public HyscaleInputSpec process(HyscaleCommandSpecBuilder commandSpec) throws HyscaleException {
        if (commandSpec == null) {
            return null;
        }
        List<File> serviceSpecs = commandSpec.getServiceSpecFiles();

        if (!serviceSpecInputValidator.validate(serviceSpecs)) {
            return null;
        }
        HyscaleInputSpec inputSpec = new HyscaleInputSpec();
        inputSpec.setServiceSpecFiles(serviceSpecs);
        List<File> profiles = commandSpec.getProfileFiles();
        String profileName = commandSpec.getProfileName();
        if (profiles == null && profileName == null) {
            return inputSpec;
        }

        if (profiles == null && profileName != null) {
            WorkflowLogger.startActivity(ControllerActivity.LOOKING_FOR_PROFILE, profileName);
            profiles = ServiceProfileUtil.getAllProfiles(serviceSpecs, profileName);
            try {
                profiles = ServiceProfileUtil.validateAndFilter(serviceSpecs, profiles, profileName);
            } catch (HyscaleException ex) {
                WorkflowLogger.endActivity(Status.FAILED);
                WorkflowLogger.logPersistedActivities();
                throw ex;
            } 
            WorkflowLogger.endActivity(Status.DONE);
            WorkflowLogger.logPersistedActivities();
        }
        
        if (!profileSpecInputValidator.validate(profiles)) {
            return null;
        }
        
        if (!validateDependency(serviceSpecs, profiles)) {
            return null;
        }
        
        inputSpec.setProfileFiles(profiles);
        
        return inputSpec;
    }
    
    private boolean validateDependency(List<File> serviceSpecFiles,
            List<File> profileFiles) throws HyscaleException {
        WorkflowLogger.startActivity(ValidatorActivity.VALIDATING_MAPPING);
        
        List<String> serviceFromSpec = new ArrayList<String>();
        List<String> serviceFromProfiles = new ArrayList<String>();

        for (File serviceSpecFile : serviceSpecFiles) {
            serviceFromSpec.add(ServiceSpecUtil.getServiceName(serviceSpecFile));
        }
        Set<String> multipleProfilesServices = new HashSet<String>();
        Set<String> profileWithoutServices = new HashSet<String>();
        
        if (profileFiles != null && !profileFiles.isEmpty()) {
            for (File profileFile : profileFiles) {
                String serviceName = ServiceProfileUtil.getServiceNameFromProfile(profileFile);
                if (serviceFromProfiles.contains(serviceName)) {
                    multipleProfilesServices.add(serviceName);
                } else {
                    serviceFromProfiles.add(serviceName);
                }
                if (!serviceFromSpec.contains(serviceName)) {
                    profileWithoutServices.add(serviceName);
                }
            }
        }
        
        if (!profileWithoutServices.isEmpty()) {
            String invalidServices = profileWithoutServices.toString();
            logger.error("Services {} mentioned in profiles not available in deployment", invalidServices);
            WorkflowLogger.endActivity(Status.FAILED);
            WorkflowLogger.error(ControllerActivity.NO_SERVICE_FOUND_FOR_PROFILE, invalidServices);
            throw new HyscaleException(ControllerErrorCodes.SERVICE_NOT_PROVIDED_FOR_PROFILE, invalidServices);
        }
        
        if (!multipleProfilesServices.isEmpty() ) {
            String invalidServices = multipleProfilesServices.toString();
            logger.error("Multiple profiles found for services {}", invalidServices);
            WorkflowLogger.endActivity(Status.FAILED);
            WorkflowLogger.error(ControllerActivity.MULIPLE_PROFILES_FOUND, invalidServices);
            throw new HyscaleException(ControllerErrorCodes.UNIQUE_PROFILE_REQUIRED, invalidServices);
        }
        
        WorkflowLogger.endActivity(Status.DONE);
        return true;
    }

}
