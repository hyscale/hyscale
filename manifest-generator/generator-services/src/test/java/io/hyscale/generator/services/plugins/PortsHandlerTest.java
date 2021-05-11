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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.reflect.TypeToken;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.utils.NormalizationUtil;
import io.hyscale.generator.services.builder.DefaultPortsBuilder;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.GsonSnippetConvertor;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1ServicePort;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

@SpringBootTest
class PortsHandlerTest {

    @Autowired
    private PortsHandler portsHandler;

    private static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice-min.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice.hspec")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice-2.hspec")));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testPortsHandler(ServiceSpec serviceSpec) throws HyscaleException, IOException {
        ManifestContext context = new ManifestContext();
        context.addGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER, ManifestResource.STATEFUL_SET.getKind());
        List<ManifestSnippet> manifestList = portsHandler.handle(serviceSpec, context);

        TypeReference<List<Port>> listTypeReference = new TypeReference<List<Port>>() {
        };
        List<Port> portList = serviceSpec.get(HyscaleSpecFields.ports, listTypeReference);
        if (portList == null || portList.isEmpty()) {
            assertTrue(CollectionUtils.isEmpty(manifestList));
            return;
        }

        verifyManifests(manifestList, portList);

    }

    private void verifyManifests(List<ManifestSnippet> manifestList, List<Port> portList) throws IOException {
        Set<V1ContainerPort> v1ContainerPorts = getContainerPorts(manifestList);
        Set<V1ServicePort> v1ServicePorts = getServicePorts(manifestList);
        for (Port port : portList) {
            String[] portAndProtocol = port.getPort().split(ToolConstants.PORTS_PROTOCOL_SEPARATOR);
            String protocol = portAndProtocol.length > 1 ? DefaultPortsBuilder.ServiceProtocol.fromString(portAndProtocol[1]).name()
                    : DefaultPortsBuilder.ServiceProtocol.TCP.name();
            int portValue = Integer.valueOf(portAndProtocol[0]);
            String portName = NormalizationUtil
                    .normalize(portAndProtocol[0] + ManifestGenConstants.NAME_DELIMITER + protocol);
            assertTrue(v1ContainerPorts.stream()
                    .anyMatch(containerPort -> containerPort.getContainerPort().equals(portValue)
                            && containerPort.getProtocol().equals(protocol)
                            && containerPort.getName().equals(portName)));

            assertTrue(v1ServicePorts.stream().anyMatch(servicePort -> servicePort.getPort().equals(portValue)
                    && servicePort.getProtocol().equals(protocol) && servicePort.getName().equals(portName)));
        }

    }

    private Set<V1ServicePort> getServicePorts(List<ManifestSnippet> manifestList) throws IOException {
        for (ManifestSnippet manifest : manifestList) {
            if (manifest.getPath().equals("spec.ports")) {
                return GsonSnippetConvertor.deserialize(manifest.getSnippet(), new TypeToken<Set<V1ServicePort>>() {
                }.getType());
            }
        }

        return null;
    }

    private Set<V1ContainerPort> getContainerPorts(List<ManifestSnippet> manifestList) throws IOException {
        for (ManifestSnippet manifest : manifestList) {
            if (manifest.getPath().equals("spec.template.spec.containers[0].ports")) {
                return GsonSnippetConvertor.deserialize(manifest.getSnippet(), new TypeToken<Set<V1ContainerPort>>() {
                }.getType());
            }
        }

        return null;
    }
}
