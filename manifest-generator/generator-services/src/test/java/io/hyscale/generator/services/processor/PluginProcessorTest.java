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
package io.hyscale.generator.services.processor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestNode;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestMeta;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Test to ensure snippets are generated for each expected resource
 *
 */
@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
@Ignore
class PluginProcessorTest {

    @Autowired
    private PluginProcessor pluginProcessor;

    private ManifestContext context = new ManifestContext();

    @BeforeAll
    public void init() {
        context.setAppName("appName");
    }

    private static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/agents/agents-deploy.hspec"),
                        getResourceList(ManifestResource.DEPLOYMENT, ManifestResource.SERVICE,
                                ManifestResource.CONFIG_MAP, ManifestResource.SECRET)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/agents/agents-only.hspec"),
                        getResourceList(ManifestResource.STATEFUL_SET, ManifestResource.SERVICE)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/agents/agents-props.hspec"),
                        getResourceList(ManifestResource.STATEFUL_SET, ManifestResource.SERVICE,
                                ManifestResource.CONFIG_MAP)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/agents/agents-secrets.hspec"),
                        getResourceList(ManifestResource.STATEFUL_SET, ManifestResource.SERVICE,
                                ManifestResource.SECRET)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/agents/agents.hspec"),
                        getResourceList(ManifestResource.STATEFUL_SET, ManifestResource.SERVICE,
                                ManifestResource.CONFIG_MAP, ManifestResource.CONFIG_MAP, ManifestResource.SECRET,
                                ManifestResource.SECRET)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/deployment.hspec"),
                        getResourceList(ManifestResource.DEPLOYMENT, ManifestResource.SERVICE)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/noservice.hspec"),
                        getResourceList(ManifestResource.STATEFUL_SET)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice.hspec"),
                        getResourceList(ManifestResource.STATEFUL_SET, ManifestResource.SERVICE,
                                ManifestResource.HORIZONTAL_POD_AUTOSCALER, ManifestResource.CONFIG_MAP, 
                                ManifestResource.SECRET,ManifestResource.GATEWAY,ManifestResource.VIRTUAL_SERVICE,
                                ManifestResource.DESTINATION_RULE)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/service-hpa-deploy.hspec"),
                        getResourceList(ManifestResource.DEPLOYMENT, ManifestResource.SERVICE,
                                ManifestResource.HORIZONTAL_POD_AUTOSCALER)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/service-props.hspec"),
                        getResourceList(ManifestResource.STATEFUL_SET, ManifestResource.SERVICE,
                                ManifestResource.CONFIG_MAP)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/service-secrets.hspec"),
                        getResourceList(ManifestResource.STATEFUL_SET, ManifestResource.SERVICE,
                                ManifestResource.SECRET)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/service.hspec"),
                        getResourceList(ManifestResource.STATEFUL_SET, ManifestResource.SERVICE,
                                ManifestResource.CONFIG_MAP, ManifestResource.SECRET)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/processor/statefulset.hspec"),
                        getResourceList(ManifestResource.STATEFUL_SET, ManifestResource.SERVICE)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice-min.hspec"),
                        getResourceList(ManifestResource.DEPLOYMENT))
                );
    }

    @ParameterizedTest
    @MethodSource("input")
    void test(ServiceSpec serviceSpec, List<String> expectedResources) {
        Map<ManifestMeta, ManifestNode> manifestMap = pluginProcessor.process(serviceSpec, context);

        List<String> generatedResources = manifestMap.entrySet().stream().map(each -> each.getKey().getKind())
                .collect(Collectors.toList());
        assertTrue(CollectionUtils.isEqualCollection(expectedResources, generatedResources));
    }

    private static List<String> getResourceList(ManifestResource... manifestResources) {
        List<String> resourceList = new ArrayList<>();
        for (ManifestResource manifestResource : manifestResources) {
            resourceList.add(manifestResource.getKind());
        }
        return resourceList;
    }

}
