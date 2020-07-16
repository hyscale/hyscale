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
package io.hyscale.generator.services.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.config.SetupConfig;

@Component
public class ManifestConfig {

	@Autowired
	private SetupConfig setupConfig;

	private static final String MANIFESTS_DIR = "manifests";

	public String getManifestDir(String appName, String serviceName) {
		StringBuilder sb = new StringBuilder();
		sb.append(setupConfig.getGeneratedFilesDir(appName, serviceName)).append(MANIFESTS_DIR)
				.append(SetupConfig.FILE_SEPARATOR);
		return sb.toString();
	}

}
