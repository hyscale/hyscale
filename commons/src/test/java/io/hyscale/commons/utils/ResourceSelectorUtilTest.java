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
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ResourceSelectorUtilTest {
    private static final String APP_NAME = "myApp";
    private static final String SVC_NAME = "mySvc";

    public static Stream<Arguments> getLabelInputs() {
        Map<ResourceLabelKey, String> testMap = new HashMap<>();
        testMap.put(ResourceLabelKey.SERVICE_NAME, SVC_NAME);
        return Stream.of(Arguments.of(null, null),
                Arguments.of(new HashMap<>(), null),
                Arguments.of(testMap, ResourceLabelKey.SERVICE_NAME.getLabel() + "=" + SVC_NAME));
    }

    @ParameterizedTest
    @MethodSource(value = "getLabelInputs")
    public void testGetSelectorFromLabelMap(Map inputLabel, String expectedSelector) {
        assertEquals(ResourceSelectorUtil.getSelectorFromLabelMap(inputLabel), expectedSelector);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/appData.csv", numLinesToSkip = 1)
    public void testGetSelectorApp(String appName, String expApp) {
        String test = ResourceSelectorUtil.getSelector(appName);
        Map selector = getSelectorMap(test);
        assertEquals(expApp, selector.get(ResourceLabelKey.APP_NAME.getLabel()));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/appEnvData.csv", numLinesToSkip = 1)
    public void testGetSelectorAppAndEnv(String appName, String envName, String expApp, String expEnv) {
        Map selector = getSelectorMap(ResourceSelectorUtil.getSelector(appName, envName));
        assertEquals(expEnv, selector.get(ResourceLabelKey.ENV_NAME.getLabel()));
        assertEquals(expApp, selector.get(ResourceLabelKey.APP_NAME.getLabel()));
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/appEnvSvcData.csv", numLinesToSkip = 1)
    public void testGetSelectorAppEnvSvc(String appName, String envName, String svcName, String expApp, String expEnv, String expSvc) {
        Map selector = getSelectorMap(ResourceSelectorUtil.getSelector(appName, envName, svcName));
        assertEquals(expEnv, selector.get(ResourceLabelKey.ENV_NAME.getLabel()));
        assertEquals(expApp, selector.get(ResourceLabelKey.APP_NAME.getLabel()));
        assertEquals(expSvc, selector.get(ResourceLabelKey.SERVICE_NAME.getLabel()));
    }

    @Test
    public void testGetServiceSelector() {
        Map selector = getSelectorMap(ResourceSelectorUtil.getServiceSelector(APP_NAME, SVC_NAME));
        assertNotNull(selector);
        assertEquals(APP_NAME, selector.get(ResourceLabelKey.APP_NAME.getLabel()));
        assertEquals(SVC_NAME, selector.get(ResourceLabelKey.SERVICE_NAME.getLabel()));
    }

    private Map<String, String> getSelectorMap(String selectorString) {
        Map selector = new HashMap();
        System.out.println(selectorString);
        if(selectorString == null){
            return selector;
        }
        String[] values = selectorString.split(",");
        for (String each : values) {
            String[] keyValue = each.split("=");
            selector.put(keyValue[0], keyValue[1]);
        }
        return selector;
    }

}
