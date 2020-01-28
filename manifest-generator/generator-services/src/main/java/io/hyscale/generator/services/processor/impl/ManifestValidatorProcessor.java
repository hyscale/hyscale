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
package io.hyscale.generator.services.processor.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.processor.ManifestInterceptorProcessor;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.model.service.Volume;

/**
 * Processor to validate service spec before manifest generation
 * @author tushar
 *
 */
@Component
public class ManifestValidatorProcessor extends ManifestInterceptorProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ManifestValidatorProcessor.class);

    @Override
    protected void _preProcess(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException {
        logger.debug("Executing Manifest Validator Processor");
        if (serviceSpec == null) {
            logger.debug("Empty service spec found at manifest validator processor ");
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_REQUIRED);
        }
        TypeReference<List<Port>> listTypeReference = new TypeReference<List<Port>>() {
        };
        List<Port> portList = serviceSpec.get(HyscaleSpecFields.ports, listTypeReference);

        boolean validate = true;
        if (portList != null && !portList.isEmpty()) {
            for (Port port : portList) {
                validate = validate && port != null && StringUtils.isNotBlank(port.getPort());
                logger.debug("Port : {}", port.getPort());
                if (!validate) {
                    logger.debug("Error validating ports of service spec");
                    throw new HyscaleException(ManifestErrorCodes.INVALID_PORTS_FOUND);
                }
            }
        }

        TypeReference<List<Volume>> volumeTypeReference = new TypeReference<List<Volume>>() {
        };
        List<Volume> volumeList = serviceSpec.get(HyscaleSpecFields.volumes, volumeTypeReference);
        if (volumeList != null && !volumeList.isEmpty()) {
            for (Volume volume : volumeList) {
                validate = validate && volume != null && StringUtils.isNotBlank(volume.getName())
                        && StringUtils.isNotBlank(volume.getPath());
                if (!validate) {
                    logger.debug("Error validating volumes of service spec");
                    throw new HyscaleException(ManifestErrorCodes.INVALID_VOLUMES_FOUND);
                }
            }
        }
    }

    @Override
    protected void _postProcess(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException {
    }

    @Override
    protected void _onError(ServiceSpec serviceSpec, ManifestContext context, Throwable th) throws HyscaleException {
        if (th != null && th instanceof HyscaleException) {
            HyscaleException hex = (HyscaleException) th;
            logger.error("Inside on error method in {}", getClass().toString(), hex.getMessage());
            throw hex;
        }
    }

}
