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

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

public class ResourceSelectorUtilTests {
    private static final String APP_NAME = "myApp";
    private static final String SVC_NAME ="mySvc";
    private static final String ENV_NAME = "myEnv";

    @Test
    public void testGetSelectorFromLabelMap() {
        assertNull(ResourceSelectorUtil.getSelectorFromLabelMap(null));
    }

    @Test
    public void testGetSelectorApp() {
        assertEquals(ResourceLabelKey.APP_NAME.getLabel() + "=" + APP_NAME, ResourceSelectorUtil.getSelector(APP_NAME));
    }

    @Test
    public void testGetSelectorAppAndEnv() {
        Map selector = getSelectorMap(ResourceSelectorUtil.getSelector(APP_NAME, ENV_NAME));
        assertNotNull(selector);
        assertEquals(ENV_NAME, selector.get(ResourceLabelKey.ENV_NAME.getLabel()));
        assertEquals(APP_NAME, selector.get(ResourceLabelKey.APP_NAME.getLabel()));
    }

    @Test
    public void testGetSelectorAppEnvSvc() {
        Map selector = getSelectorMap(ResourceSelectorUtil.getSelector(APP_NAME, ENV_NAME, SVC_NAME));
        assertNotNull(selector);
        assertEquals(ENV_NAME, selector.get(ResourceLabelKey.ENV_NAME.getLabel()));
        assertEquals(APP_NAME, selector.get(ResourceLabelKey.APP_NAME.getLabel()));
        assertEquals(SVC_NAME, selector.get(ResourceLabelKey.SERVICE_NAME.getLabel()));
    }

    @Test
    public void testGetServiceSelector() {
        Map selector = getSelectorMap(ResourceSelectorUtil.getServiceSelector(APP_NAME, SVC_NAME));
        assertNotNull(selector);
        assertEquals(APP_NAME, selector.get(ResourceLabelKey.APP_NAME.getLabel()));
        assertEquals(SVC_NAME, selector.get(ResourceLabelKey.SERVICE_NAME.getLabel()));
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
