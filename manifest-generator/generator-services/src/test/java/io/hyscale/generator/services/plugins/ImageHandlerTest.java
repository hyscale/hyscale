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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.utils.NormalizationUtil;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.model.ResourceName;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.util.ImageUtil;

@SpringBootTest
class ImageHandlerTest {

    @Autowired
    private ImageHandler imageHandler;

    private static final String IMAGE_SHA_ID = "12345678";

    private static final String IMAGE_PULL_SECRET_NAME = "ImageSecrets";

    private static final List<String> MANIFEST_PATHS = Arrays.asList("spec.template.spec.containers[0].imagePullPolicy",
            "spec.template.spec.containers[0].image", "spec.template.spec.imagePullSecrets");

    private static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/image/no-image.hspec"), getContext(false)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice.hspec"), getContext(false)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice.hspec"), getContext(true)));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testImageHandler(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException, IOException {
        List<ManifestSnippet> manifestList = imageHandler.handle(serviceSpec, context);
        String imageName = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.name),
                String.class);
        if (StringUtils.isBlank(imageName)) {
            assertTrue(CollectionUtils.isEmpty(manifestList));
            return;
        }
        verfiyManifests(manifestList, serviceSpec, context);
    }

    private void verfiyManifests(List<ManifestSnippet> manifestList, ServiceSpec serviceSpec, ManifestContext context)
            throws HyscaleException, IOException {
        // Verify all manifests are generated
        List<String> manifestPaths = manifestList.stream().map(each -> each.getPath()).collect(Collectors.toList());
        assertTrue(CollectionUtils.isEqualCollection(manifestPaths, MANIFEST_PATHS));

        // Verify content of manifest
        for (ManifestSnippet manifestSnippet : manifestList) {
            if (manifestSnippet.getPath().equals("spec.template.spec.containers[0].image")) {
                String imageShaId = (String) context.getGenerationAttribute(ManifestGenConstants.IMAGE_SHA_SUM);
                String image = StringUtils.isNotBlank(imageShaId)
                        ? ImageUtil.getImageWithDigest(serviceSpec, imageShaId)
                        : ImageUtil.getImage(serviceSpec);
                assertEquals(image, manifestSnippet.getSnippet());
            }

            if (manifestSnippet.getPath().equals("spec.template.spec.containers[0].imagePullPolicy")) {
                assertEquals(ManifestGenConstants.DEFAULT_IMAGE_PULL_POLICY, manifestSnippet.getSnippet());
            }

            if (manifestSnippet.getPath().equals("spec.template.spec.imagePullSecrets")) {
                List<ResourceName> resourceNameList = new ArrayList<>();
                ResourceName resourceName = new ResourceName();
                resourceName.setName(NormalizationUtil.normalize(IMAGE_PULL_SECRET_NAME));
                resourceNameList.add(resourceName);
                TypeReference<List<ResourceName>> list = new TypeReference<List<ResourceName>>() {
                };
                CollectionUtils.isEqualCollection(resourceNameList,
                        JsonSnippetConvertor.deserialize(manifestSnippet.getSnippet(), list));
            }
        }
    }

    private static ManifestContext getContext(boolean withSHAID) {
        ManifestContext context = new ManifestContext();
        if (withSHAID) {
            context.addGenerationAttribute(ManifestGenConstants.IMAGE_SHA_SUM, IMAGE_SHA_ID);
        }
        context.addGenerationAttribute(ManifestGenConstants.IMAGE_PULL_SECRET_NAME, IMAGE_PULL_SECRET_NAME);
        context.addGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER, ManifestResource.STATEFUL_SET.getKind());
        return context;

    }
}
