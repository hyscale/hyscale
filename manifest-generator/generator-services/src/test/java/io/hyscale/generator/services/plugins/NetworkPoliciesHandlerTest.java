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
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NetworkPoliciesHandlerTest {

    @Autowired
    NetworkPoliciesHandler networkPoliciesHandler;

    private static final Logger logger = LoggerFactory.getLogger(NetworkPoliciesHandlerTest.class);

    // This method sends parameters to the test method
    private static Stream<Arguments> input() throws HyscaleException {
        try {
            ClassLoader classloader = NetworkPoliciesHandlerTest.class.getClassLoader();
            return Stream.of(
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/network-policies/input/input-1.hspec"),
                            IOUtils.toString(Objects.requireNonNull(classloader.getResourceAsStream("plugins/network-policies/output/output-1.yaml")), StandardCharsets.UTF_8)),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/network-policies/input/input-2.hspec"),
                            IOUtils.toString(Objects.requireNonNull(classloader.getResourceAsStream("plugins/network-policies/output/output-2.yaml")), StandardCharsets.UTF_8)),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/network-policies/input/input-3.hspec"),
                            IOUtils.toString(Objects.requireNonNull(classloader.getResourceAsStream("plugins/network-policies/output/output-3.yaml")), StandardCharsets.UTF_8)),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/network-policies/input/input-4.hspec"),
                            IOUtils.toString(Objects.requireNonNull(classloader.getResourceAsStream("plugins/network-policies/output/output-4.yaml")), StandardCharsets.UTF_8)),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/network-policies/input/input-5.hspec"),
                            IOUtils.toString(Objects.requireNonNull(classloader.getResourceAsStream("plugins/network-policies/output/output-5.yaml")), StandardCharsets.UTF_8)),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/network-policies/input/input-6.hspec"),
                            IOUtils.toString(Objects.requireNonNull(classloader.getResourceAsStream("plugins/network-policies/output/output-6.yaml")), StandardCharsets.UTF_8)),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/network-policies/input/input-7.hspec"),
                            IOUtils.toString(Objects.requireNonNull(classloader.getResourceAsStream("plugins/network-policies/output/output-7.yaml")), StandardCharsets.UTF_8)));
        } catch (Exception e) {
            HyscaleException ex = new HyscaleException(e, ManifestErrorCodes.ERROR_WHILE_CREATING_MANIFEST);
            logger.error("Error while generating Manifest Files", ex);
            throw ex;
        }
    }

    @ParameterizedTest
    @MethodSource("input")
    void testManifestGeneration(ServiceSpec serviceSpec, String output) throws HyscaleException {
        ManifestContext manifestContext = ManifestContextTestUtil.getManifestContext(null);
        // Generate Manifests with Service Spec
        List<ManifestSnippet> manifestSnippetList = networkPoliciesHandler.handle(serviceSpec, manifestContext);
        //Assert the Generated to Expected
        verifyManifests(manifestSnippetList, output);
    }

    public void verifyManifests(List<ManifestSnippet> manifestSnippetList, String output) {

        logger.info("Verifying Manifest and Yaml : {} ", output.replaceAll("\\s", "").equals(manifestSnippetList.get(0).getSnippet().replaceAll("\\s", "")));
        Assertions.assertEquals(manifestSnippetList.get(0).getSnippet().replaceAll("\\s", ""), output.replaceAll("\\s", ""));
    }
}
