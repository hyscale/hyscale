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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.plugins.ResourceLimitsHandler.ValueRange;
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.GsonSnippetConvertor;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;

@SpringBootTest
class ResourceLimitHandlerTest {

    @Autowired
    private ResourceLimitsHandler resourceLimitsHandler;

    private static final String CPU = "cpu";
    private static final String MEMORY = "memory";

    private static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/resources/cpu-memory.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/resources/cpu.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/resources/memory.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice-min.hspec")));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testResourceHandler(ServiceSpec serviceSpec) throws HyscaleException, IOException {
        ManifestContext context = ManifestContextTestUtil.getManifestContext(ManifestResource.STATEFUL_SET);
        List<ManifestSnippet> manifestList = resourceLimitsHandler.handle(serviceSpec, context);
        assertFalse(CollectionUtils.isEmpty(manifestList));

        verifyManifest(manifestList.get(0), serviceSpec);
    }

    private void verifyManifest(ManifestSnippet manifestSnippet, ServiceSpec serviceSpec)
            throws HyscaleException, IOException {
        assertEquals("spec.template.spec.containers[0].resources", manifestSnippet.getPath());

        V1ResourceRequirements resourceRequirements = GsonSnippetConvertor.deserialize(manifestSnippet.getSnippet(),
                V1ResourceRequirements.class);
        String memory = serviceSpec.get(HyscaleSpecFields.memory, String.class);
        String cpu = serviceSpec.get(HyscaleSpecFields.cpu, String.class);

        validateResource(resourceRequirements, cpu, CPU);
        validateResource(resourceRequirements, memory, MEMORY);
    }

    private void validateResource(V1ResourceRequirements resourceRequirements, String resource, String key) {
        if (StringUtils.isNotBlank(resource)) {
            ValueRange cpuRange = getValueRange(resource);
            assertTrue(cpuRange.getMin() == null ? resourceRequirements.getRequests() == null
                    : cpuRange.getMin().equals(resourceRequirements.getRequests().get(key)));
            assertEquals(cpuRange.getMax(), resourceRequirements.getLimits().get(key));
        } else {
            assertTrue(
                    resourceRequirements.getRequests() == null || !resourceRequirements.getRequests().containsKey(key));
            assertTrue(resourceRequirements.getLimits() == null || !resourceRequirements.getLimits().containsKey(key));
        }
    }

    private ValueRange getValueRange(String value) {
        ValueRange range = new ValueRange();
        if (value.contains("-")) {
            int separatorIndex = value.indexOf("-");
            range.setMin(Quantity.fromString(value.substring(0, separatorIndex)));
            range.setMax(Quantity.fromString(value.substring(separatorIndex + 1)));
        } else {
            range.setMax(Quantity.fromString(value));
        }
        return range;
    }
}
