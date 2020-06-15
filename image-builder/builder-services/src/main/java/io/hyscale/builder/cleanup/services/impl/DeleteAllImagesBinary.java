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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import io.hyscale.builder.cleanup.services.DeleteAllImages;
import io.hyscale.builder.services.impl.DockerBinaryImpl;
import io.hyscale.builder.services.spring.DockerBinaryCondition;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Class provides a docker binary based implementation of {@link DeleteAllImages}
 * <p>
 * docker rmi $(docker images --filter label=imageowner=hyscale -q)
 */


@Component
@Conditional(DockerBinaryCondition.class)
public class DeleteAllImagesBinary extends DeleteAllImages {

    private static final Logger logger = LoggerFactory.getLogger(DeleteAllImagesBinary.class);

    @Autowired
    private ImageCommandProvider imageCommandProvider;
    
    @Autowired
    private DockerBinaryImpl dockerBinaryImpl;

    @Override
    public void clean(ServiceSpec serviceSpec) {
        logger.debug("Cleaning up all images");
        // Fetch the image id's to be deleted which are labelled by imageowner=hyscale
        String existingImageIds = CommandExecutor.executeAndGetResults(
                imageCommandProvider.dockerImagesFilterByImageOwner()).getCommandOutput();
        String[] imageIds = StringUtils.isNotBlank(existingImageIds) ? existingImageIds.split("\\s+") : null;
        if (imageIds == null || imageIds.length == 0) {
            logger.debug("No images found to clean from the host machine");
            return;
        }
        List<String> imageIdList = Arrays.asList(imageIds);
        logger.debug("Removing images: {}", imageIdList);
        dockerBinaryImpl.deleteImages(imageIdList, false);
    }
}
