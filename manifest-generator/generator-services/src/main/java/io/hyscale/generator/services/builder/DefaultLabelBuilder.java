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

import io.hyscale.commons.models.ResourceLabelKey;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.commons.utils.ResourceLabelBuilder;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

//TODO Normalize label as per regex (([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9])?
public class DefaultLabelBuilder {
    
    private DefaultLabelBuilder() {}

    public static Map<String, String> build(ServiceMetadata serviceMetadata) {
        if (serviceMetadata == null) {
            return null;
        }
        Map<ResourceLabelKey, String> resourceLabelMap = ResourceLabelBuilder.build(serviceMetadata.getAppName(), serviceMetadata.getEnvName(),
                serviceMetadata.getServiceName());
        return build(resourceLabelMap);
    }

    public static Map<String, String> build(String appName, String envName, String serviceName) {
        Map<ResourceLabelKey, String> resourceLabelMap = ResourceLabelBuilder.build(appName, envName,
                serviceName);
        return build(resourceLabelMap);
    }

    public static Map<String, String> build(String appName, String envName) {
        Map<ResourceLabelKey, String> resourceLabelMap = ResourceLabelBuilder.build(appName, envName);
        return build(resourceLabelMap);
    }
    
    public static Map<String, String> build(Map<ResourceLabelKey, String> resourceLabelMap){
        if (resourceLabelMap == null || resourceLabelMap.isEmpty()) {
            return null;
        }
        return resourceLabelMap.entrySet().stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(k -> k.getKey().getLabel(), Entry::getValue));
    }

}
