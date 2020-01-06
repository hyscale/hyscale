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
package io.hyscale.deployer.services.util;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.utils.ContextNormalizer;
import io.hyscale.deployer.services.processor.DeployerInterceptorProcessor;
import org.springframework.stereotype.Component;

@Component
public class DeployerNormalizationProcessor extends DeployerInterceptorProcessor {

    @Override
    protected void _preProcess(DeploymentContext context) throws HyscaleException {
        ContextNormalizer.getNormalizedContext(context);
    }

    @Override
    protected void _postProcess(DeploymentContext context) throws HyscaleException {

    }

    @Override
    protected void _onError(DeploymentContext context) throws HyscaleException {

    }
}
