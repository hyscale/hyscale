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
package io.hyscale.builder.cleanup.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import io.hyscale.builder.cleanup.services.PreserveAll;
import io.hyscale.builder.services.spring.DockerClientCondition;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Class provides a docker REST API based implementation of {@link PreserveAll}
 */
@Component
@Conditional(DockerClientCondition.class)
public class PreserveAllRESTClient extends PreserveAll {

    private static final Logger logger = LoggerFactory.getLogger(PreserveAllRESTClient.class);

    @Override
    public void clean(ServiceSpec serviceSpec) {
        // No clean up  required
        logger.debug("Preserving all service images");
    }
}
