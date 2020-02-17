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
package io.hyscale.controller.hooks;

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.component.InvokerHook;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.model.service.Volume;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Hook to validate service spec before manifest generation
 *
 */
@Component
public class ManifestValidatorHook implements InvokerHook<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(ManifestValidatorHook.class);

    @Override
    public void preHook(WorkflowContext context) throws HyscaleException {
        logger.debug("Executing Manifest Validator Hook");
        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            logger.debug("Empty service spec found at manifest validator hook ");
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_REQUIRED);
        }
        TypeReference<List<Port>> listTypeReference = new TypeReference<List<Port>>() {
        };
        List<Port> portList = serviceSpec.get(HyscaleSpecFields.ports, listTypeReference);

        boolean validate = true;
        if (portList != null && !portList.isEmpty()) {
            for (Port port : portList) {
                logger.debug("Port : {}",port.getPort());
                validate = validate && port != null && StringUtils.isNotBlank(port.getPort());
                if (!validate) {
                    logger.debug("Error validating ports of service spec");
                    throw new HyscaleException(ControllerErrorCodes.INVALID_PORTS_FOUND);
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
                    throw new HyscaleException(ControllerErrorCodes.INVALID_VOLUMES_FOUND);
                }
            }
        }
    }

    @Override
    public void postHook(WorkflowContext context) throws HyscaleException {

    }

    @Override
    public void onError(WorkflowContext context, Throwable th) {
        context.setFailed(true);
    }
}
