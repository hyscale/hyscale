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

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class ResourceSelectorUtilTests {
    private static String appName;
    private static String svcName;
    private static String envName;

    @BeforeAll
    public static void init() {
        appName = "myApp";
        svcName = "mySvc";
        envName = "myEnv";
    }

    @Test
    public void testGetSelectorFromLabelMap() {
        assertNull(ResourceSelectorUtil.getSelectorFromLabelMap(null));
    }

    @Test
    public void testGetSelectorApp() {
        assertEquals(ResourceLabelKey.APP_NAME.getLabel() + "=" + appName, ResourceSelectorUtil.getSelector(appName));
    }

    @Test
    public void testGetSelectorAppAndEnv() {
        Map selector = getSelectorMap(ResourceSelectorUtil.getSelector(appName, envName));
        assertNotNull(selector);
        assertEquals(envName, selector.get(ResourceLabelKey.ENV_NAME.getLabel()));
        assertEquals(appName, selector.get(ResourceLabelKey.APP_NAME.getLabel()));
    }

    @Test
    public void testGetSelectorAppEnvSvc() {
        Map selector = getSelectorMap(ResourceSelectorUtil.getSelector(appName, envName, svcName));
        assertNotNull(selector);
        assertEquals(envName, selector.get(ResourceLabelKey.ENV_NAME.getLabel()));
        assertEquals(appName, selector.get(ResourceLabelKey.APP_NAME.getLabel()));
        assertEquals(svcName, selector.get(ResourceLabelKey.SERVICE_NAME.getLabel()));
    }

    @Test
    public void testGetServiceSelector() {
        Map selector = getSelectorMap(ResourceSelectorUtil.getServiceSelector(appName, svcName));
        assertNotNull(selector);
        assertEquals(appName, selector.get(ResourceLabelKey.APP_NAME.getLabel()));
        assertEquals(svcName, selector.get(ResourceLabelKey.SERVICE_NAME.getLabel()));
    }

    private Map<String, String> getSelectorMap(String selectorString) {
        String[] values = selectorString.split(",");
        Map selector = new HashMap();
        for (String each : values
        ) {
            String[] keyValue = each.split("=");
            selector.put(keyValue[0].trim(), keyValue[1].trim());
        }
        return selector;
    }
}
