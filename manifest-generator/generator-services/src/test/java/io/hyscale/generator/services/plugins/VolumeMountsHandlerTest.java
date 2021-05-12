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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.model.service.Volume;
import io.kubernetes.client.openapi.models.V1VolumeMount;

@SpringBootTest
class VolumeMountsHandlerTest {

    @Autowired
    private VolumeMountsHandler volumeMountsHandler;

    private static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/volumes/no-props-vol.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/volumes/no-secrets-vol.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice-min.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/volumes/props-vol.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/volumes/secrets-vol.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/volumes/volumes.hspec")));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testVolumeMountsHandler(ServiceSpec serviceSpec) throws HyscaleException, IOException {
        TypeReference<List<Volume>> volumesList = new TypeReference<List<Volume>>() {
        };
        List<Volume> volumes = serviceSpec.get(HyscaleSpecFields.volumes, volumesList);
        ManifestContext context = CollectionUtils.isEmpty(volumes)
                ? ManifestContextTestUtil.getManifestContext(ManifestResource.DEPLOYMENT)
                : ManifestContextTestUtil.getManifestContext(ManifestResource.STATEFUL_SET);

        List<ManifestSnippet> manifestList = volumeMountsHandler.handle(serviceSpec, context);

        assertTrue(!manifestList.isEmpty());

        verifyManifest(manifestList.get(0), serviceSpec);
    }

    private void verifyManifest(ManifestSnippet manifestSnippet, ServiceSpec serviceSpec)
            throws HyscaleException, IOException {
        List<String> expectedMountPaths = getExpectedMountPaths(serviceSpec);
        assertEquals("spec.template.spec.containers[0].volumeMounts", manifestSnippet.getPath());

        TypeReference<List<V1VolumeMount>> typeReference = new TypeReference<List<V1VolumeMount>>() {
        };
        List<V1VolumeMount> volumeMounts = JsonSnippetConvertor.deserialize(manifestSnippet.getSnippet(),
                typeReference);

        if (CollectionUtils.isEmpty(expectedMountPaths)) {
            assertTrue(CollectionUtils.isEmpty(volumeMounts));
            return;
        }

        assertTrue(CollectionUtils.isEqualCollection(expectedMountPaths,
                volumeMounts.stream().map(each -> each.getMountPath()).collect(Collectors.toList())));
    }

    private List<String> getExpectedMountPaths(ServiceSpec serviceSpec) throws HyscaleException {
        List<String> expectedMountPaths = new ArrayList<>();
        TypeReference<List<Volume>> volumesList = new TypeReference<List<Volume>>() {
        };
        List<Volume> volumes = serviceSpec.get(HyscaleSpecFields.volumes, volumesList);
        if (CollectionUtils.isNotEmpty(volumes)) {
            volumes.stream().forEach(volume -> expectedMountPaths.add(volume.getPath()));
        }
        if (ManifestPredicates.haveConfigmapVolume().test(serviceSpec)
                && ManifestResource.CONFIG_MAP.getPredicate().test(serviceSpec)) {
            expectedMountPaths.add(serviceSpec.get(HyscaleSpecFields.propsVolumePath, String.class));
        }
        if (ManifestPredicates.haveSecretsVolume().test(serviceSpec)
                && ManifestResource.SECRET.getPredicate().test(serviceSpec)) {
            expectedMountPaths.add(serviceSpec.get(HyscaleSpecFields.secretsVolumePath, String.class));
        }
        return expectedMountPaths;
    }
}
