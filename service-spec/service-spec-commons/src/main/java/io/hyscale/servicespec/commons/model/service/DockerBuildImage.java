package io.hyscale.servicespec.commons.model.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerBuildImage extends Image {

	private String name;
	private String registry;
	private String tag;
	private DockerSpec dockerSpec;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRegistry() {
		return registry;
	}

	public void setRegistry(String registry) {
		this.registry = registry;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public DockerSpec getDockerSpec() {
		return dockerSpec;
	}

	public void setDockerSpec(DockerSpec dockerSpec) {
		this.dockerSpec = dockerSpec;
	}

}
