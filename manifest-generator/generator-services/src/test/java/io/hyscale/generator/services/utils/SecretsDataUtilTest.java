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
package io.hyscale.generator.services.utils;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.MapBasedSecrets;
import io.hyscale.servicespec.commons.model.service.SecretType;
import io.hyscale.servicespec.commons.model.service.Secrets;
import io.hyscale.servicespec.commons.model.service.SetBasedSecrets;

class SecretsDataUtilTest {

    private static Stream<Arguments> input() {
        return Stream.of(Arguments.of(null, null, null),
                Arguments.of(getSecret(SecretType.SET, "test", null), null, null),
                Arguments.of(getSecret(SecretType.MAP, "test", "test"), null, null),
                Arguments.of(getSecret(SecretType.MAP, "test", "test"), "test", "test.txt"));
    }

    @ParameterizedTest
    @MethodSource("input")
    void secretsDataTest(Secrets secrets, String secretsVolumePath, String fileName) {
        ManifestSnippet snippet = null;
        try {
            snippet = SecretsDataUtil.build(secrets, secretsVolumePath, fileName);
        } catch (JsonProcessingException e) {
            fail(e);
        }
        if (secrets == null || secrets.getType() == SecretType.SET) {
            assertNull(snippet);
            return;
        }
        MapBasedSecrets mapBasedSecrets = (MapBasedSecrets) secrets;
        try {
            Map modifiedMap = JsonSnippetConvertor.deserialize(snippet.getSnippet(), Map.class);
            assertTrue(mapBasedSecrets.entrySet().stream().allMatch(each -> modifiedMap.containsKey(each.getKey())));
            if (StringUtils.isNotBlank(secretsVolumePath)) {
                assertTrue(modifiedMap.containsKey(fileName));
            }
        } catch (IOException e) {
            fail(e);
        }
    }
    
    private static Secrets getSecret(SecretType type, String key, String value) {
        if (type == SecretType.SET) {
            SetBasedSecrets secret = new SetBasedSecrets();
            secret.add(key);
            return secret;
        }
        MapBasedSecrets secret = new MapBasedSecrets();
        secret.put(key, value);
        return secret;
    }
}
