package io.hyscale.commons.models;

import java.io.File;
import java.util.List;

public class DockerfileEntity {

	private File dockerfile;
	private List<SupportingFile> supportingFileList;

	public File getDockerfile() {
		return dockerfile;
	}

	public void setDockerfile(File dockerfile) {
		this.dockerfile = dockerfile;
	}

	public List<SupportingFile> getSupportingFileList() {
		return supportingFileList;
	}

	public void setSupportingFileList(List<SupportingFile> supportingFileList) {
		this.supportingFileList = supportingFileList;
	}

}
