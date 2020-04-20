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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.model.HyscaleCommandSpec;
import io.hyscale.controller.model.HyscaleInputSpec;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.ServiceProfileUtil;
import io.hyscale.controller.util.ServiceSpecUtil;
import io.hyscale.controller.validator.impl.ClusterValidator;
import io.hyscale.controller.validator.impl.DockerDaemonValidator;
import io.hyscale.controller.validator.impl.ManifestValidator;
import io.hyscale.controller.validator.impl.ProfileSpecInputValidator;
import io.hyscale.controller.validator.impl.RegistryValidator;
import io.hyscale.controller.validator.impl.ServiceSpecInputValidator;
import io.hyscale.controller.validator.impl.VolumeValidator;

@Component
public class HyscaleInputSpecProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(HyscaleInputSpecProcessor.class);

    /**
     * This class extracts out common functionality between manifest generation and deployer
     * Especially related to service and profile spec validation and processing
     * 
     * Also provides a list of post processors
     */

    @Autowired
    private ServiceSpecInputValidator serviceSpecInputValidator;
    
    @Autowired
    private ProfileSpecInputValidator profileSpecInputValidator;
    
    @Autowired
    private DockerDaemonValidator dockerValidator;
    
    @Autowired
    private RegistryValidator registryValidator;
    
    @Autowired
    private ManifestValidator manifestValidator;
    
    @Autowired
    private ClusterValidator clusterValidator;
    
    @Autowired
    private VolumeValidator volumeValidator;
    
    private List<Validator<WorkflowContext>> generateManifestPostValidators = new ArrayList<Validator<WorkflowContext>>();
    
    private List<Validator<WorkflowContext>> deployPostValidators = new ArrayList<Validator<WorkflowContext>>();
    
    @PostConstruct
    public void init() {
        // Post validators
        deployPostValidators.add(dockerValidator);
        deployPostValidators.add(registryValidator);
        deployPostValidators.add(manifestValidator);
        deployPostValidators.add(clusterValidator);
        deployPostValidators.add(volumeValidator);
        
        generateManifestPostValidators.add(manifestValidator);
    }
    
    public List<Validator<WorkflowContext>> getManifestPostValidators() {
        return generateManifestPostValidators;
    }
    
    public List<Validator<WorkflowContext>> getDeployPostValidators() {
        return deployPostValidators;
    }

    public HyscaleInputSpec process(HyscaleCommandSpec commandSpec) throws HyscaleException {
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
        if (profiles == null && commandSpec.getProfileName() == null) {
            return inputSpec;
        }

        if (profiles == null && commandSpec.getProfileName() != null) {
            profiles = ServiceProfileUtil.getAllProfiles(serviceSpecs, commandSpec.getProfileName());
            profiles = ServiceProfileUtil.validateAndFilter(serviceSpecs, profiles);
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
        Map<String, Entry<String, File>> serviceVsProfile = new HashMap<String, Map.Entry<String, File>>();
        List<String> invalidServiceList = new ArrayList<String>();
        if (profileFiles != null && !profileFiles.isEmpty()) {
            for (File profileFile : profileFiles) {
                String profileName = ServiceProfileUtil.getProfileName(profileFile);
                String serviceName = ServiceProfileUtil.getServiceNameFromProfile(profileFile);
                if (serviceVsProfile.get(serviceName) != null) {
                    // Multiple profiles for a single service
                    invalidServiceList.add(serviceName);
                }
                serviceVsProfile.put(serviceName, new SimpleEntry<String, File>(profileName, profileFile));
            }
        }

        if (!invalidServiceList.isEmpty()) {
            String invalidServices = invalidServiceList.toString();
            logger.error("Multiple profiles found for services {}", invalidServices);
            WorkflowLogger.error(ControllerActivity.MULIPLE_PROFILES_FOUND, invalidServices);
            throw new HyscaleException(ControllerErrorCodes.UNIQUE_PROFILE_REQUIRED, invalidServices);
        }

        Map<String, File> serviceVsSpecFile = new HashMap<String, File>();

        for (File serviceSpecFile : serviceSpecFiles) {
            serviceVsSpecFile.put(ServiceSpecUtil.getServiceName(serviceSpecFile), serviceSpecFile);
        }

        // Services specified in profile not found
        invalidServiceList = serviceVsProfile.entrySet().stream().map(entrySet -> entrySet.getKey())
                .filter(service -> !serviceVsSpecFile.containsKey(service)).collect(Collectors.toList());

        if (invalidServiceList != null && !invalidServiceList.isEmpty()) {
            String invalidServices = invalidServiceList.toString();
            logger.error("Services {} mentioned in profiles not available in deployment", invalidServices);
            WorkflowLogger.error(ControllerActivity.NO_SERVICE_FOUND_FOR_PROFILE, invalidServices);
            throw new HyscaleException(ControllerErrorCodes.SERVICE_NOT_PROVIDED_FOR_PROFILE, invalidServices);
        }
        return true;
    }

}
