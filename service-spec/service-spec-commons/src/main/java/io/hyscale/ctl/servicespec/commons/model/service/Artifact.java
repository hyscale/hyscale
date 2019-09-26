package io.hyscale.ctl.servicespec.commons.model.service;

public class Artifact {

	private String name;
	private ArtifactProvider provider;
	private String destination;
	private String source;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public ArtifactProvider getProvider() {
		return provider;
	}

	public void setProvider(ArtifactProvider provider) {
		this.provider = provider;
	}

	public void setProvider(String provider) {
		this.provider = ArtifactProvider.fromString(provider);
	}
}
