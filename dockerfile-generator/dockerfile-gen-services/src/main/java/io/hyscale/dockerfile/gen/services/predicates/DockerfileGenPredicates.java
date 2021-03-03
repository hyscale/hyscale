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
package io.hyscale.dockerfile.gen.services.predicates;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.predicates.ServiceSpecPredicates;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class DockerfileGenPredicates {
    
    private DockerfileGenPredicates() {}

    private static final Logger logger = LoggerFactory.getLogger(DockerfileGenPredicates.class);

    public static Predicate<ServiceSpec> skipDockerfileGen() {
        return serviceSpec -> {
            if (serviceSpec == null) {
                return false;
            }
            Dockerfile userDockerfile = null;
            BuildSpec buildSpec = null;
            try {
                userDockerfile = serviceSpec.get(
                        HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile),
                        Dockerfile.class);
                buildSpec = serviceSpec.get(    
                        HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec),    
                        BuildSpec.class);
            } catch (HyscaleException e) {
                logger.error("Error while fetching dockerfile from  image", e);
            }
            if (userDockerfile == null && buildSpec == null) {
                return true;
            }

            if (userDockerfile != null) {
                return true;
            }
            
            return ServiceSpecPredicates.stackAsServiceImage().test(serviceSpec);

        };
    }

}
