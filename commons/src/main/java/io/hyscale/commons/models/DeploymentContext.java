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

import java.util.ArrayList;
import java.util.List;

import io.hyscale.commons.annotations.Normalize;
import io.hyscale.commons.component.ComponentContext;
import io.hyscale.commons.utils.NormalizationEntity;

public class DeploymentContext extends ComponentContext {
	private List<Manifest> manifests;
	@Normalize(entity = NormalizationEntity.NAMESPACE)
	private String namespace;
	private AuthConfig authConfig;
	private boolean waitForReadiness = true;
	private boolean tailLogs;
	private Integer readLines;

	public List<Manifest> getManifests() {
		return manifests;
	}

	public void setManifests(List<Manifest> manifests) {
		this.manifests = manifests;
	}

	public String getNamespace() {
	    return namespace;
	}
	
	public void setNamespace(String namespace) {
	    this.namespace = namespace;
	}
	
	public AuthConfig getAuthConfig() {
		return authConfig;
	}

	public void setAuthConfig(AuthConfig authConfig) {
		this.authConfig = authConfig;
	}

	// Manifest helper method
	public void addManifest(Manifest manifest) {
		if (manifests == null) {
			manifests = new ArrayList<Manifest>();
		}
		manifests.add(manifest);
	}

	public boolean isWaitForReadiness() {
		return waitForReadiness;
	}

	public void setWaitForReadiness(boolean waitForReadiness) {
		this.waitForReadiness = waitForReadiness;
	}

	public boolean isTailLogs() {
		return tailLogs;
	}

	public void setTailLogs(boolean tailLogs) {
		this.tailLogs = tailLogs;
	}

	public Integer getReadLines() {
		return readLines;
	}

	public void setReadLines(Integer readLines) {
		this.readLines = readLines;
	}

}
