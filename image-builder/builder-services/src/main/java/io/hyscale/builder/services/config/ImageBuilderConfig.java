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
package io.hyscale.builder.services.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.config.SetupConfig;

@Component
public class ImageBuilderConfig {

	public static final String IMAGE_BUILDER_PROP = "hyscalctl.image.builder";
	private static final String PUSH_LOG = "push.log";
	private static final String BUILD_LOG = "build.log";
	@Autowired
	private SetupConfig setupConfig;

	public String getDockerBuildlog(String appName, String serviceName) {
		StringBuilder sb = new StringBuilder(setupConfig.getLogsDir(appName, serviceName));
		sb.append(BUILD_LOG);
		return sb.toString();
	}

	public String getDockerPushLogDir(String appName, String serviceName) {
		StringBuilder sb = new StringBuilder(setupConfig.getLogsDir(appName, serviceName));
		sb.append(PUSH_LOG);
		return sb.toString();
	}

}
