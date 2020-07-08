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
package io.hyscale.commons.models;

public enum ResourceLabelKey {
	RELEASE_VERSION("hyscale.io/release-version"), APP_NAME("hyscale.io/app-name"),
	ENV_NAME("hyscale.io/environment-name"), SERVICE_NAME("hyscale.io/service-name"),
	VOLUME_NAME("hyscale.io/volume-name"), PLATFORM_DOMAIN("hyscale.io/platform-domain"),
	HYSCALE_COMPONENT("hyscale.io/component"), HYSCALE_COMPONENT_GROUP("hyscale.io/component-group"),
	HYSCALE_CLUSTER_VERSION("hyscale.io/max-compatible-cluster-version");

	private ResourceLabelKey(String label) {
		this.label = label;
	}

	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
