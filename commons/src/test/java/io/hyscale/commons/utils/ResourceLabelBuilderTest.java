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

import io.hyscale.commons.models.ResourceLabelKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import java.util.stream.Stream;

public class ResourceLabelBuilderTest {
    private static final String APP_NAME = "myApp";
    private static final String SVC_NAME = "mySvc";

    @ParameterizedTest
    @CsvFileSource(resources = "/appEnvSvcData.csv", numLinesToSkip = 1)
    public void testBuildWithAppSvcEnv(String appName, String envName, String svcName, String expApp, String expEnv, String expSvc) {
        Map<ResourceLabelKey, String> label = ResourceLabelBuilder.build(appName, envName, svcName);
        assertEquals(expEnv, label.get(ResourceLabelKey.ENV_NAME));
        assertEquals(expApp, label.get(ResourceLabelKey.APP_NAME));
        assertEquals(expSvc, label.get(ResourceLabelKey.SERVICE_NAME));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/appEnvData.csv", numLinesToSkip = 1)
    public void testBuildWithAppEnv(String appName, String envName, String expApp, String expEnv) {
        Map<ResourceLabelKey, String> label = ResourceLabelBuilder.build(appName, envName);
        assertEquals(expApp, label.get(ResourceLabelKey.APP_NAME));
        assertEquals(expEnv, label.get(ResourceLabelKey.ENV_NAME));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/appData.csv", numLinesToSkip = 1)
    public void testBuildWithApp(String appName, String expApp) {
        Map<ResourceLabelKey, String> label = ResourceLabelBuilder.build(appName);
        assertEquals(expApp, label.get(ResourceLabelKey.APP_NAME));
    }


    @Test
    public void testBuildServiceLabel() {
        Map<ResourceLabelKey, String> label = ResourceLabelBuilder.buildServiceLabel(APP_NAME, SVC_NAME);
        assertEquals(APP_NAME, label.get(ResourceLabelKey.APP_NAME));
        assertEquals(SVC_NAME, label.get(ResourceLabelKey.SERVICE_NAME));
    }

    public static Stream<Arguments> getInputsForNormalizationTests() {
        return Stream.of(Arguments.of("normaLize", "normaLize"),
                Arguments.of("normalize@1", "normalize1"),
                Arguments.of(null, null),
                Arguments.arguments(" ", ""),
                Arguments.arguments("normalize@ 1", "normalize-1"),
                Arguments.arguments("normalize@1 ", "normalize1"),
                Arguments.arguments("normalize@1$", "normalize1"),
                Arguments.arguments("norm@3alize ", "norm3alize"),
                Arguments.arguments(" %norm@ ", "norm"));
    }

    @ParameterizedTest
    @MethodSource(value = "getInputsForNormalizationTests")
    public void testNormalize(String input, String expected) {
        assertEquals(expected, ResourceLabelBuilder.normalize(input));
    }
}
