package io.hyscale.ctl.commons.models;

import java.util.ArrayList;
import java.util.List;

public class DeploymentContext {
	private List<Manifest> manifests;
	private AuthConfig authConfig;
	private String namespace;
	private String serviceName;
	private boolean waitForReadiness = true;
	private boolean tailLogs;
	private String appName;
	private Integer readLines;

	public List<Manifest> getManifests() {
		return manifests;
	}

	public void setManifests(List<Manifest> manifests) {
		this.manifests = manifests;
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

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public boolean isWaitForReadiness() {
		return waitForReadiness;
	}

	public void setWaitForReadiness(boolean waitForReadiness) {
		this.waitForReadiness = waitForReadiness;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
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
