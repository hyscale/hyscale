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
package io.hyscale.generator.services.builder;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.hyscale.commons.models.ResourceLabelKey;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.commons.utils.ResourceLabelBuilder;

class DefaultLabelBuilderTest {

    private static Stream<Arguments> serviceMetadata() {
        return Stream.of(null, Arguments.of(getServiceMetadata(null, null, null)),
                Arguments.of(getServiceMetadata("app", null, null)),
                Arguments.of(getServiceMetadata(null, "env", null)),
                Arguments.of(getServiceMetadata(null, null, "svc")), Arguments.of(getServiceMetadata("", "", "")),
                Arguments.of(getServiceMetadata(" ", " ", " ")), Arguments.of(getServiceMetadata("app", "env", "svc")),
                Arguments.of(getServiceMetadata("app$app", "env$env", "svc$svc")),
                Arguments.of(
                        getServiceMetadata("verylongnametoensurelengthistruncatedandonlytherestictedvalueisavailable",
                                "verylongnametoensurelengthistruncatedandonlytherestictedvalueisavailable",
                                "verylongnametoensurelengthistruncatedandonlytherestictedvalueisavailable")),
                Arguments.of(getServiceMetadata("app name", "env name", "svc name")));
    }

    @ParameterizedTest
    @MethodSource("serviceMetadata")
    void testServiceMetadata(ServiceMetadata serviceMetadata) {
        Map<String, String> labels = DefaultLabelBuilder.build(serviceMetadata);
        if (serviceMetadata == null || (StringUtils.isBlank(serviceMetadata.getAppName())
                && StringUtils.isBlank(serviceMetadata.getEnvName())
                && StringUtils.isBlank(serviceMetadata.getServiceName()))) {
            assertTrue(labels == null || labels.isEmpty());
            return;
        }
        String appName = serviceMetadata.getAppName();
        assertEquals(StringUtils.isBlank(appName), !labels.containsKey(ResourceLabelKey.APP_NAME.getLabel()));
        if (StringUtils.isNotBlank(appName)) {
            assertEquals(ResourceLabelBuilder.normalize(appName), labels.get(ResourceLabelKey.APP_NAME.getLabel()));
        }

        String envName = serviceMetadata.getEnvName();
        assertEquals(StringUtils.isBlank(envName), !labels.containsKey(ResourceLabelKey.ENV_NAME.getLabel()));
        if (StringUtils.isNotBlank(envName)) {
            assertEquals(ResourceLabelBuilder.normalize(envName), labels.get(ResourceLabelKey.ENV_NAME.getLabel()));
        }

        String svcName = serviceMetadata.getServiceName();
        assertEquals(StringUtils.isBlank(svcName), !labels.containsKey(ResourceLabelKey.SERVICE_NAME.getLabel()));
        if (StringUtils.isNotBlank(svcName)) {
            assertEquals(ResourceLabelBuilder.normalize(svcName), labels.get(ResourceLabelKey.SERVICE_NAME.getLabel()));
        }
    }

    private static ServiceMetadata getServiceMetadata(String appName, String envName, String serviceName) {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setAppName(appName);
        serviceMetadata.setEnvName(envName);
        serviceMetadata.setServiceName(serviceName);
        return serviceMetadata;
    }
}
