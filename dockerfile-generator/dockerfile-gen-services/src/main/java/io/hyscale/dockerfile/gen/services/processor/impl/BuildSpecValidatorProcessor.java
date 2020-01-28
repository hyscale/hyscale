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
package io.hyscale.dockerfile.gen.services.processor.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.dockerfile.gen.services.exception.DockerfileErrorCodes;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.services.processor.DockerfileGenInterceptorProcessor;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Artifact;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Processor to validate build spec before dockerfile generation
 * 
 * @author tushar
 *
 */
@Component
public class BuildSpecValidatorProcessor extends DockerfileGenInterceptorProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BuildSpecValidatorProcessor.class);

    @Override
    protected void _preProcess(ServiceSpec serviceSpec, DockerfileGenContext context) throws HyscaleException {
        logger.debug("Executing Build Spec Validator processor");
        if (serviceSpec == null) {
            logger.debug("Empty service spec found at BuildSpec validator processor ");
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_REQUIRED);
        }

        BuildSpec buildSpec = serviceSpec
                .get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec), BuildSpec.class);
        if (buildSpec == null) {
            return;
        }
        String stackImageName = buildSpec.getStackImage();
        if (StringUtils.isBlank(stackImageName)) {
            throw new HyscaleException(DockerfileErrorCodes.CANNOT_RESOLVE_STACK_IMAGE);
        }
        
        List<Artifact> artifactList = buildSpec.getArtifacts();
        
        boolean validate = true;
        if (artifactList != null && !artifactList.isEmpty()) {
            for (Artifact each : artifactList) {
                validate = validate && StringUtils.isNotBlank(each.getName())
                        && StringUtils.isNotBlank(each.getSource())
                        && StringUtils.isNotBlank(each.getDestination());
                if (!validate) {
                    throw new HyscaleException(DockerfileErrorCodes.ARTIFACTS_FOUND_INVALID_IN_SERVICE_SPEC,
                            each.getName());
                }
            }
        }
    }

    @Override
    protected void _postProcess(ServiceSpec serviceSpec, DockerfileGenContext context) throws HyscaleException {
    }

    @Override
    protected void _onError(ServiceSpec serviceSpec, DockerfileGenContext context, Throwable th)
            throws HyscaleException {
        if (th != null && th instanceof HyscaleException) {
            HyscaleException hex = (HyscaleException) th;
            logger.error("Inside on error method in {}", getClass().toString(), hex.getMessage());
            throw hex;
        }
    }
}
