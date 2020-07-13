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
package io.hyscale.generator.services.model;

import java.util.Map;
import java.util.function.Predicate;

import io.hyscale.generator.services.builder.DefaultLabelBuilder;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.utils.NormalizationUtil;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public enum ManifestResource {

    STATEFUL_SET("StatefulSet", "apps/v1") {
        @Override
        public String getName(ServiceMetadata serviceMetadata) {
            StringBuilder sb = new StringBuilder();
            sb.append(NormalizationUtil.normalize(serviceMetadata.getServiceName()));
            return sb.toString();
        }

        @Override
        public Map<String, String> getLabels(ServiceMetadata serviceMetadata) {
            return DefaultLabelBuilder.build(serviceMetadata);

        }

        @Override
        public Predicate<ServiceSpec> getPredicate() {
            return ManifestPredicates.getVolumesPredicate();
        }
    },
    DEPLOYMENT("Deployment", "apps/v1") {
        @Override
        public String getName(ServiceMetadata serviceMetadata) {
            StringBuilder sb = new StringBuilder();
            sb.append(NormalizationUtil.normalize(serviceMetadata.getServiceName()));
            return sb.toString();
        }

        @Override
        public Map<String, String> getLabels(ServiceMetadata serviceMetadata) {
            return DefaultLabelBuilder.build(serviceMetadata);
        }

        @Override
        public Predicate<ServiceSpec> getPredicate() {
            return servicespec -> {
                return !ManifestPredicates.getVolumesPredicate().test(servicespec);
            };
        }
    },
    CONFIG_MAP("ConfigMap", "v1") {
        @Override
        public String getName(ServiceMetadata serviceMetadata) {
            StringBuilder sb = new StringBuilder();
            sb.append(NormalizationUtil.normalize(serviceMetadata.getAppName()));
            sb.append(ManifestGenConstants.NAME_DELIMITER);
            sb.append(NormalizationUtil.normalize(serviceMetadata.getServiceName()));
            return sb.toString();
        }

        @Override
        public Map<String, String> getLabels(ServiceMetadata serviceMetadata) {
            return DefaultLabelBuilder.build(serviceMetadata);
        }

        // TODO set this value to false by default on props plugin should be true
        @Override
        public Predicate<ServiceSpec> getPredicate() {
            return ManifestPredicates.getPropsPredicate();
        }

    },
    SECRET("Secret", "v1") {
        @Override
        public String getName(ServiceMetadata serviceMetadata) {
            StringBuilder sb = new StringBuilder();
            sb.append(NormalizationUtil.normalize(serviceMetadata.getAppName()));
            sb.append(ManifestGenConstants.NAME_DELIMITER);
            sb.append(NormalizationUtil.normalize(serviceMetadata.getServiceName()));
            return sb.toString();
        }

        @Override
        public Map<String, String> getLabels(ServiceMetadata serviceMetadata) {
            return DefaultLabelBuilder.build(serviceMetadata);
        }

        @Override
        public Predicate<ServiceSpec> getPredicate() {
            return ManifestPredicates.getSecretsPredicate();
        }

    },
    SERVICE("Service", "v1") {
        @Override
        public String getName(ServiceMetadata serviceMetadata) {
            return NormalizationUtil.normalize(serviceMetadata.getServiceName());
        }

        @Override
        public Map<String, String> getLabels(ServiceMetadata serviceMetadata) {
            return DefaultLabelBuilder.build(serviceMetadata);
        }

        @Override
        public Predicate<ServiceSpec> getPredicate() {
            return ManifestPredicates.getPortsPredicate();
        }

    },
    HORIZONTAL_POD_AUTOSCALER("HorizontalPodAutoscaler", "autoscaling/v1") {
        @Override
        public String getName(ServiceMetadata serviceMetadata) {
            StringBuilder sb = new StringBuilder();
            sb.append(NormalizationUtil.normalize(serviceMetadata.getAppName()));
            sb.append(ManifestGenConstants.NAME_DELIMITER);
            sb.append(NormalizationUtil.normalize(serviceMetadata.getServiceName()));
            return sb.toString();
        }

        @Override
        public Map<String, String> getLabels(ServiceMetadata serviceMetadata) {
            return DefaultLabelBuilder.build(serviceMetadata);
        }

        @Override
        public Predicate<ServiceSpec> getPredicate() {
            return ManifestPredicates.isAutoScalingEnabled();
        }
    };

    private String kind;
    private String apiVersion;

    ManifestResource(String kind, String apiVersion) {
        this.kind = kind;
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return this.kind;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public static ManifestResource fromString(String kind) {
        if (StringUtils.isBlank(kind)) {
            return null;
        }
        for (ManifestResource resourceKind : ManifestResource.values()) {
            if (resourceKind.getKind().equalsIgnoreCase(kind)) {
                return resourceKind;
            }
        }
        return null;
    }

    public abstract String getName(ServiceMetadata serviceMetadata);

    public abstract Map<String, String> getLabels(ServiceMetadata serviceMetadata);

    public abstract Predicate<ServiceSpec> getPredicate();

}
