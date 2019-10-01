package io.hyscale.ctl.servicespec.commons.model.service;

import java.util.Map;

public class Dockerfile {

	private String path;
	private String dockerfilePath;
	private String target;
	private Map<String, String> args;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDockerfilePath() {
		return dockerfilePath;
	}

	public void setDockerfilePath(String dockerfilePath) {
		this.dockerfilePath = dockerfilePath;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Map<String, String> getArgs() {
		return args;
	}

	public void setArgs(Map<String, String> args) {
		this.args = args;
	}
}
