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
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DestinationRuleBuilderTest {

    @Autowired
    private DestinationRuleBuilder destinationRuleBuilder;

    private ServiceMetadata serviceMetadata = new ServiceMetadata();

    @BeforeAll
    public void init() throws HyscaleException {
        serviceMetadata.setAppName("book-info");
        serviceMetadata.setServiceName("productpage");
        serviceMetadata.setEnvName("dev");
    }

    @Test
    void testGenerateManifest() throws HyscaleException, IOException {
        ServiceSpec serviceSpec = ServiceSpecTestUtil.getServiceSpec("/builder/input/lb-istio-with-tls.hspec");
        LoadBalancer loadBalancer = ManifestContextTestUtil.getLoadBalancerFromSpec(serviceSpec);
        ManifestSnippet manifestSnippet = destinationRuleBuilder.generateManifest(serviceMetadata, loadBalancer);
        String outputYaml = FileUtils.readFileToString(new File(GatewayBuilderTest.class.getResource("/builder/output/istio/destinationRule.yaml").getFile()), StandardCharsets.UTF_8);
        assertEquals(manifestSnippet.getSnippet().replaceAll("\\s", ""), outputYaml.replaceAll("\\s", ""));
    }

    @Test
    void skipDestinationRuleGeneration() throws HyscaleException {
        ServiceSpec serviceSpec = ServiceSpecTestUtil.getServiceSpec("/builder/input/lb-istio.hspec");
        LoadBalancer loadBalancer = ManifestContextTestUtil.getLoadBalancerFromSpec(serviceSpec);
        ManifestSnippet manifestSnippet = destinationRuleBuilder.generateManifest(serviceMetadata, loadBalancer);
        assertNull(manifestSnippet);
    }

}