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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.controller.core.exception.ControllerErrorCodes;
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

    public ServiceSpec from(File serviceSpecFile) throws HyscaleException {
        checkForFile(serviceSpecFile);
        ObjectMapper mapper = ObjectMapperFactory.yamlMapper();
        try {
            ObjectNode rootNode = (ObjectNode) mapper.readTree(serviceSpecFile);
            JsonNode updatedRootNode = effectiveServiceSpecUtil.updateFilePath(rootNode);
            ServiceSpec serviceSpec = new ServiceSpec(updatedRootNode);
            return serviceSpec;
        } catch (IOException e) {
            logger.error("Error while processing service spec ", e);
            throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_PROCESSING_FAILED, e.getMessage());
        }
    }

    public ServiceSpec from(String filepath) throws HyscaleException {
        File serviceSpecFile = new File(filepath);
        checkForFile(serviceSpecFile);
        return from(serviceSpecFile);
    }

    private boolean checkForFile(File serviceSpecFile) throws HyscaleException {
        if (serviceSpecFile == null || !serviceSpecFile.exists()) {
            throw new HyscaleException(ControllerErrorCodes.CANNOT_FIND_SERVICE_SPEC,
                    serviceSpecFile != null ? serviceSpecFile.getName() : ToolConstants.EMPTY_STRING);
        }
        return true;
    }
}
