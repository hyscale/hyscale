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
package io.hyscale.builder.core.models;

import io.hyscale.commons.models.Activity;

public enum ImageBuilderActivity implements Activity {

	IMAGE_BUILD_PUSH("Image build and push "),
	IMAGE_BUILD_STARTED("Building image "),
	BUILD_LOGS("Build Logs "),
	IMAGE_TAG("Tagging image "),
	IMAGE_PULL("Pulling image "),
	IMAGE_PUSH("Pushing image "),
	IMAGE_PUSH_LOG("Push logs "),
	LOGIN("Log in to registry "),
	DOCKER_DAEMON_NOT_RUNNING("Docker daemon not running");

	private String message;

	ImageBuilderActivity(String message) {
		this.message = message;
	}

	@Override
	public String getActivityMessage() {
		return message;
	}
}
