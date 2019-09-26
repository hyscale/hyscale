package io.hyscale.ctl.servicespec.commons.model.service;

public class BuildSpecImage extends Image {

	private String name;
	private String registry;
	private String tag;
	private BuildSpec buildSpec;

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

	public BuildSpec getBuildSpec() {
		return buildSpec;
	}

	public void setBuildSpec(BuildSpec buildSpec) {
		this.buildSpec = buildSpec;
	}

}
