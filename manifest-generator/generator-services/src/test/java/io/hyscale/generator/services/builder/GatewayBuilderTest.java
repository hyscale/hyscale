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
package io.hyscale.generator.services.builder;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GatewayBuilderTest {

    @Autowired
    private GatewayBuilder gatewayBuilder;

    private static Stream<Arguments> input() throws HyscaleException {
        try {
            return Stream.of(Arguments.of(
                    ServiceSpecTestUtil.getServiceSpec("/builder/input/lb-istio.hspec"),
                    FileUtils.readFileToString(new File(GatewayBuilderTest.class.getResource("/builder/output/istio/gateway.yaml").getFile()), StandardCharsets.UTF_8)
                    ),
                    Arguments.of(
                            ServiceSpecTestUtil.getServiceSpec("/builder/input/lb-istio-with-tls.hspec"),
                            FileUtils.readFileToString(new File(GatewayBuilderTest.class.getResource("/builder/output/istio/gateway-with-tls.yaml").getFile()), StandardCharsets.UTF_8)
                    )
            );
        } catch (Exception e) {
            HyscaleException ex = new HyscaleException(e, ManifestErrorCodes.ERROR_WHILE_CREATING_MANIFEST);
            ex.printStackTrace();
            throw ex;
        }
    }

    @ParameterizedTest
    @MethodSource("input")
    void testGenerateManifest(ServiceSpec serviceSpec, String output) throws HyscaleException {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        LoadBalancer loadBalancer = ManifestContextTestUtil.getLoadBalancerFromSpec(serviceSpec);
        ManifestSnippet manifestSnippet = gatewayBuilder.generateManifest(serviceMetadata, loadBalancer);
        assertEquals(manifestSnippet.getSnippet().replaceAll("\\s", ""), output.replaceAll("\\s", ""));
    }

}