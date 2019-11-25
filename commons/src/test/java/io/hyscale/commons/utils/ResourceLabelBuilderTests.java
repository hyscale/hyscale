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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.stream.Stream;

public class ResourceLabelBuilderTests {
    private static String appName;
    private static String svcName;
    private static String envName;
    private static Date date;
    private static Long longDate;
    @BeforeAll
    public static void init() {
        appName = "myApp";
        svcName = "mySvc";
        envName = "myEnv";
        date = Calendar.getInstance().getTime();
        longDate = date.getTime();
    }

    @Test
    public void testBuildWithAppSvcEnv() {
        Map<ResourceLabelKey, String> label = ResourceLabelBuilder.build(appName, envName, svcName);
        assertEquals(envName, label.get(ResourceLabelKey.ENV_NAME));
        assertEquals(appName, label.get(ResourceLabelKey.APP_NAME));
        assertEquals(svcName, label.get(ResourceLabelKey.SERVICE_NAME));
    }

    @Test
    public void testBuildWithAppEnv() {
        Map<ResourceLabelKey, String> label = ResourceLabelBuilder.build(appName, envName);
        assertEquals(envName, label.get(ResourceLabelKey.ENV_NAME));
        assertEquals(appName, label.get(ResourceLabelKey.APP_NAME));
    }

    @Test
    public void testBuildWithApp() {
        Map<ResourceLabelKey, String> label = ResourceLabelBuilder.build(appName);
        assertEquals(appName, label.get(ResourceLabelKey.APP_NAME));
    }

    @Test
    public void testBuild() {
        Map<ResourceLabelKey, String> label = ResourceLabelBuilder.build(appName, envName, svcName, "1", longDate);
        assertEquals(envName, label.get(ResourceLabelKey.ENV_NAME));
        assertEquals(appName, label.get(ResourceLabelKey.APP_NAME));
        assertEquals(svcName, label.get(ResourceLabelKey.SERVICE_NAME));
        assertEquals("1-" + longDate.toString(), label.get(ResourceLabelKey.RELEASE_VERSION));

    }

    @Test
    public void testBuildServiceLabel() {
        Map<ResourceLabelKey, String> label = ResourceLabelBuilder.buildServiceLabel(appName, svcName);
        assertEquals(appName, label.get(ResourceLabelKey.APP_NAME));
        assertEquals(svcName, label.get(ResourceLabelKey.SERVICE_NAME));
    }

    @Test
    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of("normaLize","normaLize"),
                Arguments.of("normalize@1","normalize1"),
                Arguments.of(null,null),
                Arguments.arguments(" ",""),
                Arguments.arguments("normalize@ 1","normalize-1"),
                Arguments.arguments("normalize@1 ","normalize1"),
                Arguments.arguments("normalize@1$","normalize1"),
                Arguments.arguments("norm@3alize ","norm3alize"),
                Arguments.arguments(" %norm@ ","norm"));
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    public void testNormalize(String input,String expected) {
        assertEquals(expected, ResourceLabelBuilder.normalize(input));
    }
}
