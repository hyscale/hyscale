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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.PropType;
import io.hyscale.servicespec.commons.model.service.Props;

class ConfigMapDataUtilTest {

    private static Stream<Arguments> input() {
        return Stream.of(Arguments.of(null, null), Arguments.of(getProps(), null),
                Arguments.of(getProps(), "test.txt"));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testConfigMap(Props props, String propsVolumePath) throws IOException {
        List<ManifestSnippet> manifestSnippetList = ConfigMapDataUtil.build(props, propsVolumePath);
        if (props == null) {
            assertTrue(manifestSnippetList == null || manifestSnippetList.isEmpty());
            return;
        }
        boolean isFileAvailable = props.getProps().entrySet().stream()
                .anyMatch(each -> PropType.FILE.getPatternMatcher().matcher(each.getValue()).matches());
        
        for (ManifestSnippet snippet : manifestSnippetList) {
            Map map = JsonSnippetConvertor.deserialize(snippet.getSnippet(), Map.class);
            if (snippet.getPath().equals("data")) {
                // All props key except file should be available in snippet
                assertTrue(props.getProps().entrySet().stream()
                        .filter(each -> PropType.ENDPOINT.getPatternMatcher().matcher(each.getValue()).matches()
                                || PropType.STRING.getPatternMatcher().matcher(each.getValue()).matches())
                        .allMatch(each -> map.containsKey(each.getKey())));
            }
            if (snippet.getPath().equals("binaryData")) {
                // If file prop available binary data should be present
                assertEquals(isFileAvailable, map != null && !map.isEmpty());
            }
        }
    }

    private static Props getProps() {
        Props props = new Props();
        Map<String, String> propsMap = new HashMap<>();
        propsMap.put("string-prop", "testValue");
        propsMap.put("endpoint-prop", "(endpoint)test");
        propsMap.put("file-prop", "(file)src/test/resources/util-input/props.txt");
        props.setProps(propsMap);
        return props;
    }
}
