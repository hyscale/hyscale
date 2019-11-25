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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

public class ResourceLabelUtilTests {
    private static Map<String, String> labels = new HashMap<String, String>();
    private static String svcName;
    private static String appName;
    private static String envName;


    @BeforeAll
    public static void initLabels() {
        svcName = "mySvc";
        appName = "myApp";
        envName = "myEnv";
        labels.put("hyscale.io/service-name", svcName);
        labels.put("hyscale.io/app-name", appName);
        labels.put("hyscale.io/environment-name", envName);
    }

    @Test
    public void testGetResourceUtils() {
        Assertions.assertEquals(ResourceLabelUtil.getServiceName(labels), svcName);
        Assertions.assertNull(ResourceLabelUtil.getServiceName(null));
        Assertions.assertNull(ResourceLabelUtil.getServiceName(new HashMap<>()));
        Assertions.assertEquals(ResourceLabelUtil.getAppName(labels), appName);
        Assertions.assertEquals(ResourceLabelUtil.getEnvName(labels), envName);
    }
}
