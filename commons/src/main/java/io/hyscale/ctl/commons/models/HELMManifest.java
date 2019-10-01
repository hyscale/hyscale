package io.hyscale.ctl.commons.models;

import java.io.File;

public class HELMManifest implements Manifest {

	private File helmFile;

	public File getHelmFile() {
		return helmFile;
	}

	public void setHelmFile(File helmFile) {
		this.helmFile = helmFile;
	}

	@Override
	public ManifestType getManifestType() {
		return ManifestType.HELM;
	}
}
