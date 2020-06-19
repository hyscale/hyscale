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
package io.hyscale.builder.services.cleanup;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.core.models.ImageCleanUpPolicy;
import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.builder.services.docker.HyscaleDockerClient;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ImageMetadataUtil;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Image;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.util.ImageUtil;

/**
 * This class is responsible for cleaning up local images
 * It uses User defined cleanup policy to clean up respective images
 * The images are fetched and cleaned up based on available docker client
 *
 */
@Component
public class ImageCleanUpProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ImageCleanUpProcessor.class);

    private static final boolean USE_FORCE = false;
    
    @Autowired
    private HyscaleDockerClient hyscaleDockerClient;

    @Autowired
    private ImageBuilderConfig imageBuilderConfig;
    
    @Autowired
    private ImageMetadataUtil imageMetadataUtil;

    public void cleanUp(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException {
        String imageCleanUpPolicy = imageBuilderConfig.getImageCleanUpPolicy();

        ImageCleanUpPolicy cleanUpPolicy = ImageCleanUpPolicy.fromString(imageCleanUpPolicy);

        logger.debug("Image cleanup Policy {}", cleanUpPolicy);

        if (cleanUpPolicy == null) {
            cleanUpPolicy = ImageCleanUpPolicy.PRESERVE_N_RECENTLY_USED;
        }

        if (cleanUpPolicy == ImageCleanUpPolicy.PRESERVE_ALL) {
            logger.debug("Preserve all policy for image cleanup, no images to clean up.");
            return;
        }

        List<String> imageIds = null;
        Map<String, String> labels = imageMetadataUtil.getImageOwnerLabel();
        String imageName = getImageName(serviceSpec, cleanUpPolicy);

        imageIds = hyscaleDockerClient.getImageIds(imageName, labels);

        if (imageIds == null || imageIds.isEmpty()) {
            logger.debug("No images to clean");
            return;
        }

        if (ImageCleanUpPolicy.PRESERVE_N_RECENTLY_USED == cleanUpPolicy) {
            if (imageIds.size() > imageBuilderConfig.getNoOfPreservedImages()) {
                imageIds = imageIds.stream().skip(imageBuilderConfig.getNoOfPreservedImages())
                        .collect(Collectors.toList());
            } else {
                logger.debug("Not enough images avaialbe for cleaning");
                return;
            }
        }

        logger.debug("Images to be removed: {}", imageIds);
        hyscaleDockerClient.deleteImages(imageIds, USE_FORCE);
    }

    private String getImageName(ServiceSpec serviceSpec, ImageCleanUpPolicy cleanUpPolicy)
            throws HyscaleException {
        Image image = serviceSpec.get(HyscaleSpecFields.image, Image.class);
        switch (cleanUpPolicy) {
        case DELETE_AFTER_BUILD:
            return ImageUtil.getImage(image);
        case PRESERVE_N_RECENTLY_USED:
            return ImageUtil.getImageWithoutTag(image);
        case DELETE_ALL:
            return null;
        default:
            return ImageUtil.getImage(image);
        }

    }

}
