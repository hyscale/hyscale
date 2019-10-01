package io.hyscale.ctl.servicespec.commons.model.service;

import java.util.List;

public class BuildSpec {

	private String stackImage;
	private List<Artifact> artifacts;
	private String configScript;
	private String configCommands;
	private String runScript;
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

	public String getConfigScript() {
		return configScript;
	}

	public void setConfigScript(String configScript) {
		this.configScript = configScript;
	}

	public String getConfigCommands() {
		return configCommands;
	}

	public void setConfigCommands(String configCommands) {
		this.configCommands = configCommands;
	}

	public String getRunScript() {
		return runScript;
	}

	public void setRunScript(String runScript) {
		this.runScript = runScript;
	}

	public String getRunCommands() {
		return runCommands;
	}

	public void setRunCommands(String runCommands) {
		this.runCommands = runCommands;
	}

}
