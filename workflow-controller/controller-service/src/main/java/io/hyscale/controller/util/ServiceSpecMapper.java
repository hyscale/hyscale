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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.HyscaleFilesUtil;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.servicespec.commons.builder.EffectiveServiceSpecBuilder;
import io.hyscale.servicespec.commons.builder.MapFieldDataProvider;
import io.hyscale.servicespec.commons.builder.ServiceInputType;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Get {@link ServiceSpec} from input which could be a file or filepath
 *
 */
@Component
public class ServiceSpecMapper {

    private static final Logger logger = LoggerFactory.getLogger(ServiceSpecMapper.class);

    @Autowired
    private EffectiveServiceSpecUtil effectiveServiceSpecUtil;

    public ServiceSpec from(File serviceSpecFile, File profileFile) throws HyscaleException {
        checkForFile(serviceSpecFile);
        String serviceSpecData = HyscaleFilesUtil.readFileData(serviceSpecFile);
        String profileData = null;
        ObjectMapper mapper = ObjectMapperFactory.yamlMapper();
        if (profileFile != null) {
            profileData = HyscaleFilesUtil.readFileData(profileFile);
            String profileName = ServiceProfileUtil.getProfileName(profileFile);
            String serviceName = ServiceSpecUtil.getServiceName(serviceSpecFile);
            if (StringUtils.isNotBlank(profileData)) {
                WorkflowLogger.startActivity(ControllerActivity.APPLYING_PROFILE_FOR_SERVICE, profileName, serviceName);
                logger.debug("Merging profile {} for service {}", profileName, serviceName);
                try {
                    MapFieldDataProvider mapFieldDataProvider = new MapFieldDataProvider();
                    
                    // Merge
                    serviceSpecData = new EffectiveServiceSpecBuilder().type(ServiceInputType.YAML).withServiceSpec(serviceSpecData)
                            .withProfile(profileData).withFieldMetaDataProvider(mapFieldDataProvider).build();
                    WorkflowLogger.endActivity(Status.DONE);
                } catch(HyscaleException e){
                    logger.error("Error while applying profile {} for service {}", profileName, serviceName, e);
                    WorkflowLogger.endActivity(Status.FAILED);
                    throw e;
                }
                mapper = ObjectMapperFactory.jsonMapper();
            } else {
                // empty profile
                WorkflowLogger.persist(ControllerActivity.PROFILE_NOT_FOUND, profileName, serviceName);
            }
        }
        
        try {
            JsonNode rootNode = mapper.readTree(serviceSpecData);
            if (WindowsUtil.isHostWindows()) {
                logger.debug("Updating service spec as host system is windows");
                rootNode = effectiveServiceSpecUtil.updateFilePath((ObjectNode) rootNode);
            }
            return new ServiceSpec(rootNode);
        } catch (IOException e) {
            logger.error("Error while processing service spec ", e);
            throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_PROCESSING_FAILED, e.getMessage());
        }
    }

    public ServiceSpec from(File serviceSpecFile) throws HyscaleException {
        return from(serviceSpecFile, null);
    }
    
    public ServiceSpec from(String serviceFilepath, String profileFilePath) throws HyscaleException {
        if (StringUtils.isBlank(serviceFilepath)) {
            throw buildException(serviceFilepath);
        }
        File serviceSpecFile = new File(serviceFilepath);
        
        File profileFile = null;
        if (StringUtils.isNotBlank(profileFilePath)) {
            profileFile = new File(profileFilePath);
        }
        return from(serviceSpecFile, profileFile);
    }

    public ServiceSpec from(String filepath) throws HyscaleException {
        if (StringUtils.isBlank(filepath)) {
            throw buildException(filepath);
        }
        File serviceSpecFile = new File(filepath);
        return from(serviceSpecFile);
    }

    private boolean checkForFile(File serviceSpecFile) throws HyscaleException {
        if (serviceSpecFile == null || !serviceSpecFile.exists()) {
            throw buildException(serviceSpecFile != null ? serviceSpecFile.getName() : ToolConstants.EMPTY_STRING);
        }
        return true;
    }

    private HyscaleException buildException(String serviceSpec) {
        return new HyscaleException(ControllerErrorCodes.CANNOT_FIND_SERVICE_SPEC,
                serviceSpec != null ? serviceSpec : ToolConstants.EMPTY_STRING);
    }
}
