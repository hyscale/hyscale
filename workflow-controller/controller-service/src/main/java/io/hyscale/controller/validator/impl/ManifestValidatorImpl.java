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
package io.hyscale.controller.validator.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.model.ManifestGenCommandSpec;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.validator.ManifestGenValidator;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.model.service.Volume;

@Component
public class ManifestValidatorImpl implements ManifestGenValidator{
    private static final Logger logger = LoggerFactory.getLogger(ManifestValidatorImpl.class);

	
	@Override
	public boolean validate(ManifestGenCommandSpec manifestGenCommandSpec) throws HyscaleException {
		 logger.debug("Executing Manifest Validator Hook");
		 //TODO here we have fetch service spec from manifestGenCommandSpec
	        ServiceSpec serviceSpec = null;
	        if (serviceSpec == null) {
	            logger.debug("Empty service spec found at manifest validator hook ");
	            return false;
	        }
	        boolean validate = true;
	        TypeReference<List<Volume>> volumeTypeReference = new TypeReference<List<Volume>>() {
	        };
	        List<Volume> volumeList = serviceSpec.get(HyscaleSpecFields.volumes, volumeTypeReference);
	        if (volumeList != null && !volumeList.isEmpty()) {
	            for (Volume volume : volumeList) {
	                validate = validate && volume != null && StringUtils.isNotBlank(volume.getName())
	                        && StringUtils.isNotBlank(volume.getPath());
	                if (!validate) {
	                    logger.debug("Error validating volumes of service spec");
	                    return false;
	                }
	            }
	        }
		return true;
	
	}
}
