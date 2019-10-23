package io.hyscale.commons.models;

import java.io.File;

public class YAMLManifest implements Manifest {

	private File yamlManifest;

	public File getYamlManifest() {
		return yamlManifest;
	}

	public void setYamlManifest(File yamlManifest) {
		this.yamlManifest = yamlManifest;
	}

	@Override
	public ManifestType getManifestType() {
		return ManifestType.YAML;
	}
}
