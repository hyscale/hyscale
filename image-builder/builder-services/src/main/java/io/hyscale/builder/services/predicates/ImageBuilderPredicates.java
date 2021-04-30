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
package io.hyscale.builder.services.predicates;

import java.io.File;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class ImageBuilderPredicates {

    private ImageBuilderPredicates() {
    }

    /**
     * Build should be skipped if dockerfile is not available
     */
    public static BiPredicate<Dockerfile, BuildContext> getSkipBuildPredicate() {
        return (dockerfile, context) -> ((dockerfile == null)
                && (context == null || context.getDockerfileEntity() == null || context.getDockerfileEntity().getDockerfile() == null));
    }

    /**
     * Build or push required when:
     * User provides docker file, or tool generates dockerfile
     * In case of stack as service image need to pull and push the image
     */
    public static BiPredicate<ServiceSpec, BuildContext> getBuildPushRequiredPredicate() {
        return (serviceSpec, context) -> {
            if (context != null) {
                if (BooleanUtils.toBoolean(context.isStackAsServiceImage())) {
                    return true;
                }
                if (context.getDockerfileEntity() != null && context.getDockerfileEntity().getDockerfile() != null) {
                    return true;
                }
            }
            if (serviceSpec == null) {
                return false;
            }
            Dockerfile dockerfile = null;
            try {
                dockerfile = serviceSpec.get(
                        HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile),
                        Dockerfile.class);
            } catch (HyscaleException e) {
                return false;
            }

            return dockerfile != null;
        };
    }

    public static Predicate<String> getDockerfileExistsPredicate() {
        return dockerfilePath -> {
            if (StringUtils.isBlank(dockerfilePath)) {
                return false;
            }
            File dockerfile = new File(dockerfilePath);
            return dockerfile.exists() && dockerfile.isFile();
        };
    }

}
