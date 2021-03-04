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
package io.hyscale.builder.services.provider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.services.predicates.ImageBuilderPredicates;
import io.hyscale.builder.services.util.DockerfileUtil;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class StackImageProvider {

    private static final Logger logger = LoggerFactory.getLogger(StackImageProvider.class);

    private static final String FROM_TAG = "FROM";

    @Autowired
    private DockerfileUtil dockerfileUtil;

    public List<String> getStackImages(ServiceSpec serviceSpec) {
        List<String> stackImages = new ArrayList<>();

        String stackImageFromBuildSpec = getStackImageFromBuildSpec(serviceSpec);
        if (stackImageFromBuildSpec != null) {
            stackImages.add(stackImageFromBuildSpec);
        } else {
            stackImages.addAll(getStackImagesFromDockerfile(serviceSpec));
        }
        return stackImages;
    }

    public String getStackImageFromBuildSpec(ServiceSpec serviceSpec) {
        if (serviceSpec == null) {
            return null;
        }
        BuildSpec buildSpec = null;
        try {
            buildSpec = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec),
                    BuildSpec.class);
        } catch (HyscaleException e) {
            logger.error("Error while getting build spec for stack image");
        }
        return getStackImage(buildSpec);
    }

    public List<String> getStackImagesFromDockerfile(ServiceSpec serviceSpec) {
        if (serviceSpec == null) {
            return new ArrayList<>();
        }
        Dockerfile dockerfile = null;
        try {
            dockerfile = serviceSpec.get(
                    HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile), Dockerfile.class);
        } catch (HyscaleException e) {
            logger.error("Error while getting dockerfile for stack image");
        }
        return getStackImages(dockerfile);
    }

    public String getStackImage(BuildSpec buildSpec) {
        if (buildSpec == null) {
            return null;
        }
        return buildSpec.getStackImage();
    }

    public List<String> getStackImages(Dockerfile dockerfile) {
        // Read dockerfile and get all images after FROM tag, include multistage dockerfile
        // Example FROM alpine:3.0

        if (dockerfile == null) {
            return Collections.emptyList();
        }
        String dockerfilePath = dockerfileUtil.getDockerfileAbsolutePath(dockerfile);
        if (!ImageBuilderPredicates.getDockerfileExistsPredicate().test(dockerfilePath)) {
            return Collections.emptyList();
        }
        Set<String> stackImages = new HashSet<>();
        try {
            List<String> fileData = Files.readAllLines(Paths.get(dockerfilePath));
            stackImages.addAll(fileData.stream().filter(each -> StringUtils.isNotBlank(each) && each.contains(FROM_TAG))
                    .map(fromLine -> {
                        //  FROM alpine:3.0
                        String[] items = fromLine.split(" ");
                        if (items.length >= 2) {
                            return items[1];
                        }
                        return null;
                    }).filter(StringUtils::isNotBlank).collect(Collectors.toSet()));
        } catch (IOException e) {
            logger.error("Error while getting stack images from dockerfile");
        }
        return new ArrayList<>(stackImages);
    }
}
