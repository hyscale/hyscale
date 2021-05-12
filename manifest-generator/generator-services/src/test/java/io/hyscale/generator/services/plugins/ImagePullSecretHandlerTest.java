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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.Auth;
import io.hyscale.commons.models.DockerConfig;
import io.hyscale.commons.models.ImageRegistry;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringBootTest
class ImagePullSecretHandlerTest {

    @Autowired
    private ImagePullSecretHandler imagePullSecretHandler;

    private List<String> EXPECTED_MANIFESTS = Arrays.asList("type", "data", "metadata", "apiVersion", "kind");

    private static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/no-registry.hspec"), getContext(false)),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/no-registry.hspec"), getContext(true)));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testPullSecrets(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException, IOException {
        List<ManifestSnippet> manifestList = imagePullSecretHandler.handle(serviceSpec, context);
        if (context.getImageRegistry() == null) {
            assertTrue(CollectionUtils.isEmpty(manifestList));
            return;
        }

        verifyManifests(manifestList, context);

    }

    private void verifyManifests(List<ManifestSnippet> manifestList, ManifestContext context) throws IOException {
        List<String> manifestsGenerated = manifestList.stream().map(each -> each.getPath())
                .collect(Collectors.toList());
        assertTrue(CollectionUtils.isEqualCollection(EXPECTED_MANIFESTS, manifestsGenerated));

        for (ManifestSnippet snippet : manifestList) {
            if (snippet.getPath().equals("type")) {
                assertEquals("kubernetes.io/dockerconfigjson", snippet.getSnippet());
            }
            if (snippet.getPath().equals("kind")) {
                assertEquals(ManifestResource.SECRET.getKind(), snippet.getSnippet());
            }
            if (snippet.getPath().equals("data")) {
                Map<String, String> dockerAuthConfigMap = JsonSnippetConvertor.deserialize(snippet.getSnippet(),
                        Map.class);
                String encodedVal = dockerAuthConfigMap.get(".dockerconfigjson");
                String expectedEncodedVal = getEncodedVal(context.getImageRegistry());
                assertEquals(expectedEncodedVal, encodedVal);
            }
        }
    }

    private String getEncodedVal(ImageRegistry imageRegistry) throws JsonProcessingException {
        ObjectMapper objectMapper = ObjectMapperFactory.jsonMapper();
        DockerConfig dockerAuthConfig = new DockerConfig();

        Auth auth = new Auth();
        auth.setAuth(imageRegistry.getToken());
        Map<String, Auth> auths = new HashMap<>();
        auths.put(imageRegistry.getUrl(), auth);
        dockerAuthConfig.setAuths(auths);

        String dockerConfigJson = objectMapper.writeValueAsString(dockerAuthConfig);
        return Base64.encodeBase64String(dockerConfigJson.getBytes());
    }

    private static ManifestContext getContext(boolean withImageRegistry) {
        ManifestContext context = new ManifestContext();
        if (withImageRegistry) {
            ImageRegistry registry = new ImageRegistry();
            registry.setName("name");
            registry.setToken("admin:admin");
            registry.setUrl("hyscale.io");
        }
        return context;

    }
}
