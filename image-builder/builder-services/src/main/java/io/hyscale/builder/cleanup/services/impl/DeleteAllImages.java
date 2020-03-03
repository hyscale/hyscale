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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.cleanup.services.ImageCleanupProcessor;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * This class removes the docker images from the host machine
 * which are built by hyscale. Hyscale adds a label to the
 * image as imageowner = hyscale. This clean up happends on all
 * those images which are tagged with the label imageowner=hyscale
 * <p>
 * docker rmi $(docker images --filter label=imageowner=hyscale -q)
 */


@Component
public class DeleteAllImages implements ImageCleanupProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeleteAllImages.class);

    @Autowired
    private ImageCommandProvider imageCommandProvider;

    @Override
    public void clean(ServiceSpec serviceSpec) {
        logger.debug("Cleaning up all images");
        // Fetch the image id's to be deleted which are labelled by imageowner=hyscale
        String[] imageIds = CommandExecutor.executeAndGetResults(imageCommandProvider.dockerImagesFilterByImageOwner()).getCommandOutput().split("\\s+");
        if (imageIds == null || imageIds.length == 0) {
            logger.debug("No images found to clean from the host machine");
            return;
        }
        // Remove the image id's using 'docker rmi' command
        CommandExecutor.execute(imageCommandProvider
                .removeDockerImages(imageIds));
    }
}
