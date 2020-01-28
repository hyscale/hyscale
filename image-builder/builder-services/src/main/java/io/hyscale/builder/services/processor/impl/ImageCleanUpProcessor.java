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
package io.hyscale.builder.services.processor.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.services.command.ImageCommandGenerator;
import io.hyscale.builder.services.processor.BuilderInterceptorProcessor;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.Status;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class ImageCleanUpProcessor extends BuilderInterceptorProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ImageCleanUpProcessor.class);

    @Autowired
    private ImageCommandGenerator imageCommandGenerator;

    @Override
    protected void _preProcess(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
    }

    @Override
    protected void _postProcess(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        logger.debug("Executing image clean up processor");
        if (serviceSpec == null) {
            logger.error(" Cannot clean up image without service specs ");
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_REQUIRED);
        }

        String serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);

        String tag = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.tag),
                String.class);
        String cleanUpCommand = imageCommandGenerator.getImageCleanUpCommand(context.getAppName(), serviceName, tag);
        logger.debug("Starting image cleanup, command {}", cleanUpCommand);
        boolean success = CommandExecutor.execute(cleanUpCommand);

        logger.debug("Image clean up {}", success ? Status.DONE.getMessage() : Status.FAILED.getMessage());
    }

    @Override
    protected void _onError(ServiceSpec serviceSpec, BuildContext context, Throwable th) throws HyscaleException {
        if (th != null && th instanceof HyscaleException) {
            HyscaleException hex = (HyscaleException) th;
            logger.error("Inside on error method in {}", getClass().toString(), hex.getMessage());
            throw hex;
        }
    }
}
