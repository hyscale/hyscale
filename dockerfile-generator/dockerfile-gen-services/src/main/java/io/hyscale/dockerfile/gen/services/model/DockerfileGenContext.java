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
package io.hyscale.dockerfile.gen.services.model;

import java.util.List;

import io.hyscale.servicespec.commons.model.service.Artifact;

public class DockerfileGenContext {

	private String appName;
	private String serviceName;
	private String version;
	private List<Artifact> effectiveArtifacts;
	private boolean skipCopy;
	private boolean stackAsServiceImage;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<Artifact> getEffectiveArtifacts() {
		return effectiveArtifacts;
	}

	public void setEffectiveArtifacts(List<Artifact> effectiveArtifacts) {
		this.effectiveArtifacts = effectiveArtifacts;
	}

	public boolean isSkipCopy() {
		return skipCopy;
	}

	public void setSkipCopy(boolean skipCopy) {
		this.skipCopy = skipCopy;
	}

	public boolean isStackAsServiceImage() {
		return stackAsServiceImage;
	}

	public void setStackAsServiceImage(boolean stackAsServiceImage) {
		this.stackAsServiceImage = stackAsServiceImage;
	}
}