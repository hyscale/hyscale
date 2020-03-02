/**
 * Copyright 2019 Pramati Prism, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.builder.cleanup.services.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.cleanup.services.ImageCleanupProcessor;
import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.util.ImageUtil;

/**
 * This class preserves the last 'n' service images built by hyscale, where
 * (n = {@link io.hyscale.builder.services.config.ImageBuilderConfig#getNoOfPreservedImages() }).
 * Hyscale adds a label to the image as imageowner = hyscale. This clean up happends on all
 * those images which are tagged with the label imageowner=hyscale
 * <p>
 * docker rmi $(docker images <serviceimage> --filter label=imageowner=hyscale -q)
 */

@Component
public class PreserveLastNUsed implements ImageCleanupProcessor {
    private static final Logger logger = LoggerFactory.getLogger(PreserveLastNUsed.class);

    @Autowired
    private ImageCommandProvider imageCommandProvider;

    @Autowired
    private ImageBuilderConfig imageBuilderConfig;

    @Override
    public void clean(ServiceSpec serviceSpec) {
        logger.debug("Cleaning up least recently used images");
        String image = null;
        try {
            image = ImageUtil.getImageWithoutTag(serviceSpec);
        } catch (HyscaleException e) {
            logger.error("Error while fetching image from service spec to clean up service images", e);
        }
        if (StringUtils.isNotBlank(image)) {
            // Fetch the image id's to be deleted of the service image which are labelled by imageowner=hyscale
            String[] imgIds = CommandExecutor.executeAndGetResults(imageCommandProvider.dockerImageByNameFilterByImageOwner(image)).
                    getCommandOutput().split("\\s+");
            // Need to preserve the order of output ,hence a LinkedHashset
            Set<String> imageIds = new LinkedHashSet<>(Arrays.asList(imgIds));
            // delete those image id's which are older than 'n' (imageBuilderConfig.getNoOfPreservedImages())
            if (imageIds.size() > imageBuilderConfig.getNoOfPreservedImages()) {
                CommandExecutor.execute(imageCommandProvider.removeDockerImages(
                        imageIds.stream().skip(imageBuilderConfig.getNoOfPreservedImages()).collect(Collectors.toSet())));
            }
        }
    }
}
