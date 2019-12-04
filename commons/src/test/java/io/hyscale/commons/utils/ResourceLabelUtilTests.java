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
    private static final String APP_NAME = "myApp";
    private static final String SVC_NAME ="mySvc";
    private static final String ENV_NAME = "myEnv";

    @BeforeAll
    public static void initLabels() {
        labels.put("hyscale.io/service-name", SVC_NAME);
        labels.put("hyscale.io/app-name", APP_NAME);
        labels.put("hyscale.io/environment-name", ENV_NAME);
    }

    @Test
    public void testGetResourceUtils() {
        Assertions.assertEquals(ResourceLabelUtil.getServiceName(labels), SVC_NAME);
        Assertions.assertNull(ResourceLabelUtil.getServiceName(null));
        Assertions.assertNull(ResourceLabelUtil.getServiceName(new HashMap<>()));
        Assertions.assertEquals(ResourceLabelUtil.getAppName(labels), APP_NAME);
        Assertions.assertEquals(ResourceLabelUtil.getEnvName(labels), ENV_NAME);
    }
}
