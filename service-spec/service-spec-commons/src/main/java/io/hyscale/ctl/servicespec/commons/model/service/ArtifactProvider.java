package io.hyscale.ctl.servicespec.commons.model.service;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines artifact source
 *
 */
public enum ArtifactProvider {

	HTTP("http"), SSH("ssh"), LOCAL("local");

	private String provider;

	private ArtifactProvider(String provider) {
		this.provider = provider;
	}

	public String getProvider() {
		return this.provider;
	}

	// Returns default value if no match found
	public static ArtifactProvider fromString(String provider) {
		if (StringUtils.isBlank(provider)) {
			return LOCAL;
		}

		for (ArtifactProvider artifactProvider : ArtifactProvider.values()) {
			if (artifactProvider.getProvider().equalsIgnoreCase(provider)) {
				return artifactProvider;
			}
		}
		return LOCAL;
	}
}
