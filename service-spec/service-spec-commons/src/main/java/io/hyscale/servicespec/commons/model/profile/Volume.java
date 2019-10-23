package io.hyscale.servicespec.commons.model.profile;

import io.hyscale.commons.models.VolumeSourceType;

public class Volume {

	private String name;
	private String storageClass;
	private double size;
	private VolumeSourceType sourceType;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStorageClass() {
		return storageClass;
	}

	public void setStorageClass(String storageClass) {
		this.storageClass = storageClass;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public VolumeSourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(VolumeSourceType sourceType) {
		this.sourceType = sourceType;
	}

}
