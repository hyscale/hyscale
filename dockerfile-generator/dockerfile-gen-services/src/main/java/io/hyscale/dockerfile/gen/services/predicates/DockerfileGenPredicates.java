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
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

public class DockerfileGenPredicates {

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
                logger.error("Error while fetching dockerfile from  image {}", e);
            }
            if (userDockerfile == null && buildSpec == null) {
                return true;
            }

            if (userDockerfile != null) {
                return true;
            }

            if (stackAsServiceImage().test(serviceSpec)) {
                return true;
            }

            return false;
        };
    }

    public static Predicate<ServiceSpec> stackAsServiceImage() {
        return serviceSpec -> {
            if (serviceSpec == null) {
                return false;
            }
            BuildSpec buildSpec = null;
            try {
                buildSpec = serviceSpec.get(
                        HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec),
                        BuildSpec.class);
            } catch (HyscaleException e) {
                logger.error("Error while fetching dockerfile from  image {}", e);
            }

            if (buildSpec == null) {
                return false;
            }
            if (!haveArtifacts().test(buildSpec) && !haveConfigCommands().test(buildSpec)
                    && !haveConfigScript().test(buildSpec) && !haveRunScript().test(buildSpec)
                    && !haveRunCommands().test(buildSpec)) {
                return true;
            }
            return false;
        };
    }

    /**
     * @return true if artifacts exist in buildspec
     */

    public static Predicate<BuildSpec> haveArtifacts() {
        return buildSpec -> {
            if (buildSpec == null) {
                return false;
            }
            return buildSpec.getArtifacts() != null && !(buildSpec.getArtifacts().isEmpty());
        };
    }

    public static Predicate<BuildSpec> haveConfigCommands() {
        return buildSpec -> {
            if (buildSpec == null) {
                return false;
            }
            return StringUtils.isNotBlank(buildSpec.getConfigCommands());
        };
    }

    public static Predicate<BuildSpec> haveRunCommands() {
        return buildSpec -> {
            if (buildSpec == null) {
                return false;
            }
            return StringUtils.isNotBlank(buildSpec.getRunCommands());
        };
    }

    public static Predicate<BuildSpec> haveConfigScript() {
        return buildSpec -> {
            if (buildSpec == null) {
                return false;
            }
            return StringUtils.isNotBlank(buildSpec.getConfigCommandsScript());
        };
    }

    public static Predicate<BuildSpec> haveRunScript() {
        return buildSpec -> {
            if (buildSpec == null) {
                return false;
            }
            return StringUtils.isNotBlank(buildSpec.getRunCommandsScript());
        };
    }
}
