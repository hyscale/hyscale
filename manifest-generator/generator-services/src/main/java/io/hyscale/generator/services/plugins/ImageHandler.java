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
package io.hyscale.generator.services.plugins;

import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "ImageHandler")
public class ImageHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImageHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext)
            throws HyscaleException {
        String image = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.name), String.class);
        if (StringUtils.isBlank(image)) {
            logger.debug("Found empty image in the service spec, cannot process ImageHandler");
            return null;
        }
        List<ManifestSnippet> snippetList = new ArrayList<>();
        snippetList.add(getImageSnippet(serviceSpec, manifestContext));
        snippetList.add(getImagePullPolicy(serviceSpec, manifestContext));
        return snippetList;
    }

    private ManifestSnippet getImagePullPolicy(ServiceSpec serviceSpec, ManifestContext manifestContext) {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setSnippet(ManifestGenConstants.DEFAULT_IMAGE_PULL_POLICY);
        String podSpecOwner = ((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.containers[0].imagePullPolicy");
        return manifestSnippet;
    }

    private ManifestSnippet getImageSnippet(ServiceSpec serviceSpec, ManifestContext manifestContext)
            throws HyscaleException {
        String imageShaId = (String) manifestContext.getGenerationAttribute(ManifestGenConstants.IMAGE_SHA_SUM);
        String image = null;
        if (StringUtils.isNotBlank(imageShaId)) {
            logger.debug("Preparing image with its digest.");
            image = ImageUtil.getImageWithDigest(serviceSpec,imageShaId);
        } else {
            logger.debug("Preparing image directly from given tag.");
            image = ImageUtil.getImage(serviceSpec);
        }
        String podSpecOwner = ((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
        ManifestSnippet imageSnippet = new ManifestSnippet();
        imageSnippet.setSnippet(image);
        imageSnippet.setPath("spec.template.spec.containers[0].image");
        imageSnippet.setKind(podSpecOwner);
        return imageSnippet;
    }
}
