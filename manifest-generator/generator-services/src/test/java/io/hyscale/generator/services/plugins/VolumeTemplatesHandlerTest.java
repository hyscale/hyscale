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

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.google.gson.reflect.TypeToken;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.GsonSnippetConvertor;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.model.service.Volume;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;

@SpringBootTest
class VolumeTemplatesHandlerTest {

    @Autowired
    private VolumeTemplatesHandler volumeTemplatesHandler;

    private static final String PVC_TEMPLATE_PATH = "spec.volumeClaimTemplates";

    private static final List<String> PATHS = Arrays.asList("spec.serviceName", PVC_TEMPLATE_PATH);

    private static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice-min.hspec"),
                        ManifestContextTestUtil.getManifestContext(ManifestResource.DEPLOYMENT)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice.hspec"),
                        ManifestContextTestUtil.getManifestContext(ManifestResource.STATEFUL_SET)));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testVolumeTemplateHandler(ServiceSpec serviceSpec, ManifestContext context)
            throws HyscaleException, IOException {

        List<ManifestSnippet> manifestList = volumeTemplatesHandler.handle(serviceSpec, context);

        TypeReference<List<Volume>> typeReference = new TypeReference<List<Volume>>() {
        };
        List<Volume> volumes = serviceSpec.get(HyscaleSpecFields.volumes, typeReference);

        if (CollectionUtils.isEmpty(volumes) || !ManifestResource.STATEFUL_SET.getKind()
                .equals(context.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER))) {
            assertTrue(CollectionUtils.isEmpty(manifestList));
            return;
        }

        assertTrue(manifestList.stream().allMatch(manifest -> PATHS.contains(manifest.getPath())));

        verifyVolume(manifestList.stream().filter(manifest -> manifest.getPath().contentEquals(PVC_TEMPLATE_PATH))
                .findFirst().get(), volumes);
    }

    private void verifyVolume(ManifestSnippet manifestList, List<Volume> volumes) throws IOException {
        List<V1PersistentVolumeClaim> volumeClaims = GsonSnippetConvertor.deserialize(manifestList.getSnippet(),
                new TypeToken<List<V1PersistentVolumeClaim>>() {
                }.getType());

        assertEquals(volumes.size(), volumeClaims.size());

        Map<String, V1PersistentVolumeClaim> pvcMap = volumeClaims.stream()
                .collect(Collectors.toMap(each -> each.getMetadata().getName(), each -> each));
        Map<String, Volume> volMap = volumes.stream().collect(Collectors.toMap(Volume::getName, volume -> volume));

        for (Entry<String, V1PersistentVolumeClaim> pvcEntrySet : pvcMap.entrySet()) {
            Volume volume = volMap.get(pvcEntrySet.getKey());
            assertNotNull(volume);

            if (StringUtils.isNotBlank(volume.getStorageClass())) {
                assertEquals(volume.getStorageClass(), pvcEntrySet.getValue().getSpec().getStorageClassName());
            }

            Quantity expectedSize = StringUtils.isBlank(volume.getSize())
                    ? Quantity.fromString(K8SRuntimeConstants.DEFAULT_VOLUME_SIZE)
                    : Quantity.fromString(volume.getSize());

            assertEquals(expectedSize, pvcEntrySet.getValue().getSpec().getResources().getRequests().get("storage"));
        }
    }
}
