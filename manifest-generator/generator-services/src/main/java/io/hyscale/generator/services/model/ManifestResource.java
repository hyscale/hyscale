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
import java.util.stream.Collectors;

import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.models.ResourceLabelKey;
import io.hyscale.commons.utils.ResourceLabelBuilder;
import io.hyscale.commons.utils.NormalizationUtil;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public enum ManifestResource {

	STATEFUL_SET("StatefulSet", "apps/v1beta2") {
		@Override
		public String getName(MetaDataContext metaDataContext) {
			StringBuilder sb = new StringBuilder();
			sb.append(NormalizationUtil.normalize(metaDataContext.getAppName()));
			sb.append(ManifestGenConstants.NAME_DELIMITER);
			sb.append(NormalizationUtil.normalize(metaDataContext.getServiceName()));
			return sb.toString();
		}

		@Override
		public Map<String, String> getLabels(MetaDataContext metaDataContext) {
			Map<ResourceLabelKey, String> podLabelKeyMap = ResourceLabelBuilder.build(metaDataContext.getAppName(),
					metaDataContext.getEnvName(), metaDataContext.getServiceName());
			Map<String, String> podLabels = podLabelKeyMap.entrySet().stream().filter(each -> {
				return each != null;
			}).collect(Collectors.toMap(k -> k.getKey().getLabel(), v -> v.getValue()));
			return podLabels;
		}

		@Override
		public Predicate<ServiceSpec> getPredicate() {
			return ManifestPredicates.getVolumesPredicate();
		}
	},
	DEPLOYMENT("Deployment", "apps/v1beta2") {
		@Override
		public String getName(MetaDataContext metaDataContext) {
			StringBuilder sb = new StringBuilder();
			sb.append(NormalizationUtil.normalize(metaDataContext.getAppName()));
			sb.append(ManifestGenConstants.NAME_DELIMITER);
			sb.append(NormalizationUtil.normalize(metaDataContext.getServiceName()));
			return sb.toString();
		}

		@Override
		public Map<String, String> getLabels(MetaDataContext metaDataContext) {
			Map<ResourceLabelKey, String> podLabelKeyMap = ResourceLabelBuilder.build(metaDataContext.getAppName(),
					metaDataContext.getEnvName(), metaDataContext.getServiceName());
			Map<String, String> podLabels = podLabelKeyMap.entrySet().stream().filter(each -> {
				return each != null;
			}).collect(Collectors.toMap(k -> k.getKey().getLabel(), v -> v.getValue()));
			return podLabels;
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
		public String getName(MetaDataContext metaDataContext) {
			StringBuilder sb = new StringBuilder();
			sb.append(NormalizationUtil.normalize(metaDataContext.getAppName()));
			sb.append(ManifestGenConstants.NAME_DELIMITER);
			sb.append(NormalizationUtil.normalize(metaDataContext.getServiceName()));
			return sb.toString();
		}

		@Override
		public Map<String, String> getLabels(MetaDataContext metaDataContext) {
			Map<ResourceLabelKey, String> podLabelKeyMap = ResourceLabelBuilder.build(metaDataContext.getAppName(),
					metaDataContext.getEnvName(), metaDataContext.getServiceName());
			Map<String, String> podLabels = podLabelKeyMap.entrySet().stream().filter(each -> {
				return each != null;
			}).collect(Collectors.toMap(k -> k.getKey().getLabel(), v -> v.getValue()));
			return podLabels;
		}

		// TODO set this value to false by default on props plugin should be true
		@Override
		public Predicate<ServiceSpec> getPredicate() {
			return ManifestPredicates.getPropsPredicate();
		}

	},
	SECRET("Secret", "v1") {
		@Override
		public String getName(MetaDataContext metaDataContext) {
			StringBuilder sb = new StringBuilder();
			sb.append(NormalizationUtil.normalize(metaDataContext.getAppName()));
			sb.append(ManifestGenConstants.NAME_DELIMITER);
			sb.append(NormalizationUtil.normalize(metaDataContext.getServiceName()));
			return sb.toString();
		}

		@Override
		public Map<String, String> getLabels(MetaDataContext metaDataContext) {
			Map<ResourceLabelKey, String> podLabelKeyMap = ResourceLabelBuilder.build(metaDataContext.getAppName(),
					metaDataContext.getEnvName(), metaDataContext.getServiceName());
			Map<String, String> podLabels = podLabelKeyMap.entrySet().stream().filter(each -> {
				return each != null;
			}).collect(Collectors.toMap(k -> k.getKey().getLabel(), v -> v.getValue()));
			return podLabels;
		}

		@Override
		public Predicate<ServiceSpec> getPredicate() {
			return ManifestPredicates.getSecretsPredicate();
		}

	},
	SERVICE("Service", "v1") {
		@Override
		public String getName(MetaDataContext metaDataContext) {
			return NormalizationUtil.normalize(metaDataContext.getServiceName());
		}

		@Override
		public Map<String, String> getLabels(MetaDataContext metaDataContext) {
			Map<ResourceLabelKey, String> podLabelKeyMap = ResourceLabelBuilder.build(metaDataContext.getAppName(),
					metaDataContext.getEnvName(), metaDataContext.getServiceName());
			Map<String, String> podLabels = podLabelKeyMap.entrySet().stream().filter(each -> {
				return each != null;
			}).collect(Collectors.toMap(k -> k.getKey().getLabel(), v -> v.getValue()));
			return podLabels;
		}

		@Override
		public Predicate<ServiceSpec> getPredicate() {
			return ManifestPredicates.getPortsPredicate();
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

	public abstract String getName(MetaDataContext metaDataContext);

	public abstract Map<String, String> getLabels(MetaDataContext metaDataContext);

	public abstract Predicate<ServiceSpec> getPredicate();

}
