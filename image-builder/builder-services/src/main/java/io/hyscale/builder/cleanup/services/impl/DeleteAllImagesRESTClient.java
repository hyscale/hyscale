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

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.model.Image;

import io.hyscale.builder.cleanup.services.DeleteAllImages;
import io.hyscale.builder.services.impl.DockerRESTClientImpl;
import io.hyscale.builder.services.spring.DockerClientCondition;
import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Class provides a docker REST API based implementation of {@link DeleteAllImages}
 */

@Component
@Conditional(DockerClientCondition.class)
public class DeleteAllImagesRESTClient extends DeleteAllImages {

    private static final Logger logger = LoggerFactory.getLogger(DeleteAllImagesRESTClient.class);

    @Autowired
    private DockerRESTClientImpl dockerRESTClientImpl;

    @Autowired
    private ImageCommandProvider imageCommandProvider;

    @Override
    public void clean(ServiceSpec serviceSpec) {
        logger.debug("Cleaning up all images");

        ListImagesCmd listImageCmd = dockerRESTClientImpl.getDockerClient().listImagesCmd()
                .withLabelFilter(imageCommandProvider.getImageLabelMap());

        List<Image> imageList = listImageCmd.exec();

        if (imageList == null || imageList.isEmpty()) {
            logger.debug("No images to clean");
            return;
        }
        List<String> imageIds = imageList.stream().map(image -> {
            return image.getId();
        }).collect(Collectors.toList());
        logger.debug("Removing images: {}", imageIds);
        dockerRESTClientImpl.deleteImages(imageIds, false);
    }
}
