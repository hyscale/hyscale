package io.hyscale.commons.models;

public enum ResourceLabelKey {
	RELEASE_VERSION("hyscale.io/release-version"), APP_NAME("hyscale.io/app-name"),
	ENV_NAME("hyscale.io/environment-name"), SERVICE_NAME("hyscale.io/service-name"),
	VOLUME_NAME("hyscale.io/volume-name"), PLATFORM_DOMAIN("hyscale.io/platform-domain"),
	HYSCALE_COMPONENT("hyscale.io/component"), HYSCALE_COMPONENT_GROUP("hyscale.io/component-group");

	private ResourceLabelKey(String label) {
		this.label = label;
	}

	private String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
