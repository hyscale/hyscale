package io.hyscale.servicespec.commons.model.service;

public class DockerSpec {

	private String path;
	private String dockerfile;
	private String target;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDockerfile() {
		return dockerfile;
	}

	public void setDockerfile(String dockerfile) {
		this.dockerfile = dockerfile;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

}
