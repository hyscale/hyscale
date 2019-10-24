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
package io.hyscale.servicespec.commons.model.service;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines artifact source
 *
 */
public enum ArtifactProvider {

	HTTP("http"), SSH("ssh"), LOCAL("local");

	private String provider;

	private ArtifactProvider(String provider) {
		this.provider = provider;
	}

	public String getProvider() {
		return this.provider;
	}

	// Returns default value if no match found
	public static ArtifactProvider fromString(String provider) {
		if (StringUtils.isBlank(provider)) {
			return LOCAL;
		}

		for (ArtifactProvider artifactProvider : ArtifactProvider.values()) {
			if (artifactProvider.getProvider().equalsIgnoreCase(provider)) {
				return artifactProvider;
			}
		}
		return LOCAL;
	}
}
