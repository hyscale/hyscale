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