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

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.generator.services.builder.IngressManifestBuilder;
import io.hyscale.generator.services.builder.IstioManifestBuilder;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoadBalancerHandlerTest {

    @Autowired
    private LoadBalancerHandler loadBalancerHandler;

    @MockBean
    private IngressManifestBuilder ingressManifestBuilder;

    @MockBean
    private IstioManifestBuilder istioManifestBuilder;

    @BeforeAll
    void init() throws HyscaleException {
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        manifestSnippetList.add(new ManifestSnippet());
        Mockito.when(ingressManifestBuilder.build(ArgumentMatchers.any(ServiceMetadata.class), ArgumentMatchers.any())).thenReturn(manifestSnippetList);
        Mockito.when(istioManifestBuilder.build(ArgumentMatchers.any(ServiceMetadata.class), ArgumentMatchers.any())).thenReturn(manifestSnippetList);
    }

    public static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/builder/input/lb-istio.hspec"), false),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/builder/input/lb-nginx.hspec"), false),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/builder/input/lb-traefik.hspec"), false),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice-min.hspec"), true));
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    void testHandle(ServiceSpec serviceSpec, boolean expectedResult) throws HyscaleException {
        ManifestContext manifestContext = ManifestContextTestUtil.getManifestContext(ManifestResource.DEPLOYMENT);
        assertEquals(expectedResult, loadBalancerHandler.handle(serviceSpec, manifestContext).isEmpty());
    }
}