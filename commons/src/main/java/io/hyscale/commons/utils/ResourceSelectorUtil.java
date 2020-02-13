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

import java.util.Map;
import java.util.stream.Collectors;

import io.hyscale.commons.models.ResourceFieldSelectorKey;
import io.hyscale.commons.models.ResourceLabelKey;

public class ResourceSelectorUtil {

    public static String getSelectorFromLabelMap(Map<ResourceLabelKey, String> label) {
        if (label == null || label.isEmpty()) {
            return null;
        }
        return label.entrySet().stream().map((entry) -> entry.getKey().getLabel() + "=" + entry.getValue())
                .collect(Collectors.joining(","));
    }

    public static String getSelector(String appName, String envName, String serviceName) {
        return getSelectorFromLabelMap(ResourceLabelBuilder.build(appName, envName, serviceName));
    }

    public static String getServiceSelector(String appName, String serviceName) {
        return getSelectorFromLabelMap(ResourceLabelBuilder.buildServiceLabel(appName, serviceName));
    }

    public static String getSelector(String appName, String envName) {
        return getSelectorFromLabelMap(ResourceLabelBuilder.build(appName, envName));
    }

    public static String getSelector(String appName) {
        return getSelectorFromLabelMap(ResourceLabelBuilder.build(appName));
    }

}
