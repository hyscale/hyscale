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
package io.hyscale.builder.services.service;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.core.models.DockerImage;
import io.hyscale.builder.services.exception.ImageBuilderErrorCodes;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public interface ImagePushService {

    /**
     * Check docker exists, pull(if required), tag, push(if required)
     *
     * @param serviceSpec
     * @param buildContext
     * @throws HyscaleException
     */
    default void pushImage(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {
        validate(serviceSpec, buildContext);
        _push(serviceSpec, buildContext);
    }

    void _push(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException;

    default void validate(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {
        ArrayList<String> missingFields = new ArrayList<String>();

        if (serviceSpec == null) {
            missingFields.add("ServiceSpec");
        }
        if (buildContext == null) {
            missingFields.add("BuildContext");
        }

        if (!missingFields.isEmpty()) {
            String[] missingFieldsArr = new String[missingFields.size()];
            missingFieldsArr = missingFields.toArray(missingFieldsArr);
            throw new HyscaleException(ImageBuilderErrorCodes.FIELDS_MISSING, missingFieldsArr);
        }

        String registryUrl = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class);
        if (buildContext.getImageRegistry() == null && registryUrl != null) {
            throw new HyscaleException(ImageBuilderErrorCodes.MISSING_DOCKER_REGISTRY_CREDENTIALS, registryUrl, registryUrl);
        }
    }

    default String getSourceImageName(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException {

        if (buildContext.isStackAsServiceImage()) {
            return serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec,
                    HyscaleSpecFields.stackImage), String.class);
        }
        DockerImage dockerImage = buildContext.getDockerImage();

        return StringUtils.isNotBlank(dockerImage.getTag())
                ? dockerImage.getName() + ToolConstants.COLON + dockerImage.getTag()
                : dockerImage.getName();
    }

}