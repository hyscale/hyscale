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

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GatewayBuilderTest {

    @Autowired
    private GatewayBuilder gatewayBuilder;

    @Test
    void generateManifest() throws HyscaleException {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        LoadBalancer loadBalancer = prepareLoadBalancerFromSpec(ServiceSpecTestUtil.getServiceSpec("/builder/lb-istio.hspec"));
        ManifestSnippet snippet = gatewayBuilder.generateManifest(serviceMetadata,loadBalancer);
        assertNotNull(snippet);
    }

    LoadBalancer prepareLoadBalancerFromSpec(ServiceSpec serviceSpec) throws HyscaleException {
        LoadBalancer loadBalancer = serviceSpec.get(HyscaleSpecFields.loadBalancer, new TypeReference<LoadBalancer>(){});
        return loadBalancer;
    }
}