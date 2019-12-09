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
package io.hyscale.commons.utils;
import io.hyscale.commons.exception.HyscaleException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class MustacheTemplateResolverTests {
    private static URL sampleTemplateUrl;
    private static String TESTING_TEMPLATE_CONTENT = "ONLINE";
    private static String TESTING_TEMPLATE_KEY = "USER_STATUS";
    private static MustacheTemplateResolver mustacheTemplateResolver;
    private static String sampleTemplatePath;
    private static Map<String, Object> statusMap;

    @BeforeAll
    public static void input() {
        sampleTemplateUrl = MustacheTemplateResolverTests.class.getResource("/sampleConfig.tpl");
        sampleTemplatePath = sampleTemplateUrl.getPath();
        statusMap = new HashMap<>();
        statusMap.put(TESTING_TEMPLATE_KEY, TESTING_TEMPLATE_CONTENT);
        mustacheTemplateResolver = new MustacheTemplateResolver();
    }

    public static Stream<Arguments> getNullInputs(){
        return Stream.of(Arguments.of(null, statusMap),
                Arguments.of("", statusMap),
                Arguments.of(sampleTemplatePath, null));
    }

    @ParameterizedTest
    @MethodSource(value = "getNullInputs")
    public void testNullConditions(String templatePath, Map<String, Object> statusMap) {
        Assertions.assertThrows(HyscaleException.class, () -> {
            mustacheTemplateResolver.resolveTemplate(templatePath, statusMap);
        });
    }

    @Test
    public void testResolveTemplate() {
        String template = null;
        try {
            template = mustacheTemplateResolver.resolveTemplate(sampleTemplatePath, statusMap);
        } catch (HyscaleException e) {
            Assertions.fail(e.getMessage());
        }
        Assertions.assertNotNull(template);
        Assertions.assertEquals(template.trim(), TESTING_TEMPLATE_CONTENT);
    }
}
