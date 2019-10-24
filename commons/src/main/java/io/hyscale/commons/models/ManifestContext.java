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

import java.util.HashMap;
import java.util.Map;

public class ManifestContext {

	private String appName;
	private String envName;
	private String namespace;
	private ImageRegistry imageRegistry;
	private Map<String, Object> generationAttributes;

	public ManifestContext() {
		this.generationAttributes = new HashMap<>();
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public ImageRegistry getImageRegistry() {
		return imageRegistry;
	}

	public void setImageRegistry(ImageRegistry imageRegistry) {
		this.imageRegistry = imageRegistry;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public Object getGenerationAttribute(String key) {
		return generationAttributes.get(key);
	}

	public void addGenerationAttribute(String key, Object value) {
		generationAttributes.put(key, value);
	}

}
