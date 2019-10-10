package io.hyscale.ctl.commons.models;

public enum VolumeAccessMode {
	READ_ONLY("ReadOnlyMany", true), READ_WRITE_ONCE("ReadWriteOnce", false), READ_WRITE_MANY("ReadWriteMany", false);

	private String accessMode;
	private boolean readOnly;

	VolumeAccessMode(String accessMode, boolean readOnly) {
		this.accessMode = accessMode;
		this.readOnly = readOnly;
	}

	public String getAccessMode() {
		return this.accessMode;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}
}