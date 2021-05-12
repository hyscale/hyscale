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
import java.util.List;
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
import io.kubernetes.client.openapi.models.V1Volume;

@SpringBootTest
class VolumesHandlerTest {

    @Autowired
    private VolumesHandler volumesHandler;

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
    void testVolumesHandler(ServiceSpec serviceSpec) throws HyscaleException, IOException {
        TypeReference<List<Volume>> volumesList = new TypeReference<List<Volume>>() {
        };
        List<Volume> volumes = serviceSpec.get(HyscaleSpecFields.volumes, volumesList);
        ManifestContext context = CollectionUtils.isEmpty(volumes)
                ? ManifestContextTestUtil.getManifestContext(ManifestResource.DEPLOYMENT)
                : ManifestContextTestUtil.getManifestContext(ManifestResource.STATEFUL_SET);

        List<ManifestSnippet> manifestList = volumesHandler.handle(serviceSpec, context);

        assertTrue(!manifestList.isEmpty());

        verifyManifest(manifestList.get(0), serviceSpec);
    }

    private void verifyManifest(ManifestSnippet manifestSnippet, ServiceSpec serviceSpec) throws IOException {
        assertEquals("spec.template.spec.volumes", manifestSnippet.getPath());
        TypeReference<List<V1Volume>> typeReference = new TypeReference<List<V1Volume>>() {
        };
        List<V1Volume> snippetVolumes = JsonSnippetConvertor.deserialize(manifestSnippet.getSnippet(), typeReference);

        if (ManifestPredicates.haveConfigmapVolume().test(serviceSpec)
                && ManifestPredicates.getPropsPredicate().test(serviceSpec)) {
            assertTrue(snippetVolumes.stream().anyMatch(vol -> vol.getConfigMap() != null));
        } else {
            assertTrue(snippetVolumes == null || snippetVolumes.stream().allMatch(vol -> vol.getConfigMap() == null));
        }

        if (ManifestPredicates.haveSecretsVolume().test(serviceSpec)
                && ManifestPredicates.getSecretsEnvPredicate().test(serviceSpec)) {
            assertTrue(snippetVolumes.stream().anyMatch(vol -> vol.getSecret() != null));
        } else {
            assertTrue(snippetVolumes == null || snippetVolumes.stream().allMatch(vol -> vol.getSecret() == null));
        }
    }
}
