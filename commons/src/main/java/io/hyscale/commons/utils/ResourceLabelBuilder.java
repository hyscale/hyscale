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

import java.util.HashMap;
import java.util.Map;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.models.ResourceLabelKey;

public class ResourceLabelBuilder {

	private static final Integer MAX_LABEL_VALUE_SIZE = 63;

	public static Map<ResourceLabelKey, String> build(String appName, String envName, String serviceName) {
		Map<ResourceLabelKey, String> labels = new HashMap<ResourceLabelKey, String>();

		if (StringUtils.isNotBlank(appName)) {
			labels.put(ResourceLabelKey.APP_NAME, normalize(appName));
		}
		if (StringUtils.isNotBlank(envName)) {
			labels.put(ResourceLabelKey.ENV_NAME, normalize(envName));
		}
		if (StringUtils.isNotBlank(serviceName)) {
			labels.put(ResourceLabelKey.SERVICE_NAME, normalize(serviceName));
		}
		return labels;
	}

	public static Map<ResourceLabelKey, String> buildServiceLabel(String appName, String serviceName) {
		return build(appName, null, serviceName);
	}

	public static Map<ResourceLabelKey, String> build(String appName) {
		return build(appName, null, null);
	}

	public static Map<ResourceLabelKey, String> build(String appName, String envName) {
		return build(appName, envName, null);
	}

	public static Map<ResourceLabelKey, String> buildSystem(String appName, String envName) {
		Map<ResourceLabelKey, String> labels = build(appName, envName, null);
		labels.put(ResourceLabelKey.HYSCALE_COMPONENT, normalize(K8SRuntimeConstants.HYSCALE_SYSTEM_COMPONENT));
		return labels;
	}

	public static Map<ResourceLabelKey, String> build(String appName, String envName, String serviceName,
			String releaseVersion, Long createdAt) {
		Map<ResourceLabelKey, String> labels = build(appName, envName, serviceName);
		if (StringUtils.isNotBlank(releaseVersion)) {
			labels.put(ResourceLabelKey.RELEASE_VERSION,
					normalize(releaseVersion) + (createdAt != null ? "-" + createdAt.toString() : ""));
		}

		return labels;
	}

	public static String normalize(String name) {
		if (name == null) {
			return null;
		}
		String normalized = name.replaceAll(" ", "").replaceAll("[\\.]+", "-").replaceAll("[ ]+", "-")
				.replaceAll("[^a-zA-Z0-9-_]", "");
		return normalized.substring(0, Integer.min(MAX_LABEL_VALUE_SIZE - 1, normalized.length()));
	}
}
