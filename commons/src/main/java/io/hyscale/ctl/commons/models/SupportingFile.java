package io.hyscale.ctl.commons.models;

import java.io.File;

public class SupportingFile {

	private FileSpec fileSpec;
	private File file;
	// Relative path to docker
	private String relativePath;

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public FileSpec getFileSpec() {
		return fileSpec;
	}

	public void setFileSpec(FileSpec fileSpec) {
		this.fileSpec = fileSpec;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
