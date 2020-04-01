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
package io.hyscale.controller.processors;

import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.commons.component.PrePostProcessors;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Hook to validate image details in service spec
 *
 */
@Component
public class ImageValidatorProcessor implements PrePostProcessors<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(ImageValidatorProcessor.class);

    @Override
    public void preProcess(WorkflowContext context) throws HyscaleException {
        logger.debug("Executing {}", getClass());
        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_REQUIRED);
        }
        String imageName = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.name), String.class);
        if (StringUtils.isBlank(imageName)) {
            throw new HyscaleException(ImageBuilderErrorCodes.CANNOT_RESOLVE_IMAGE_NAME);
        }
    }

    @Override
    public void postProcess(WorkflowContext context) throws HyscaleException {

    }

    @Override
    public void onError(WorkflowContext context, Throwable th) {
        context.setFailed(true);
    }
}
