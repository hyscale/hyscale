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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildSpec {

	private String stackImage;
	private List<Artifact> artifacts;
	private String configCommandsScript;
	private String configCommands;
	private String runCommandsScript;
	private String runCommands;

	public String getStackImage() {
		return stackImage;
	}

	public void setStackImage(String stackImage) {
		this.stackImage = stackImage;
	}

	public List<Artifact> getArtifacts() {
		return artifacts;
	}

	public void setArtifacts(List<Artifact> artifacts) {
		this.artifacts = artifacts;
	}

	public String getConfigCommands() {
		return configCommands;
	}

	public void setConfigCommands(String configCommands) {
		this.configCommands = configCommands;
	}

	public String getRunCommands() {
		return runCommands;
	}

	public void setRunCommands(String runCommands) {
		this.runCommands = runCommands;
	}

	public String getConfigCommandsScript() {
		return configCommandsScript;
	}

	public void setConfigCommandsScript(String configCommandsScript) {
		this.configCommandsScript = configCommandsScript;
	}

	public String getRunCommandsScript() {
		return runCommandsScript;
	}

	public void setRunCommandsScript(String runCommandsScript) {
		this.runCommandsScript = runCommandsScript;
	}

}
