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
