package io.hyscale.ctl.commons.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileMeta implements Serializable {

	private String fileId;
	private String fileName;
	private String fileKey;
	private StorageType storageType;

	public FileMeta() {

	}

	public FileMeta(String fileId, String fileName, String fileKey, StorageType storageType) {
		this.fileId = fileId;
		this.fileName = fileName;
		this.fileKey = fileKey;
		this.storageType = storageType;
	}

	public FileMeta(String fileName, String fileKey, StorageType storageType) {
		this.fileName = fileName;
		this.fileKey = fileKey;
		this.storageType = storageType;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@JsonIgnore
	public String getFileKey() {
		return fileKey;
	}

	public void setFileKey(String fileKey) {
		this.fileKey = fileKey;
	}

	@JsonIgnore
	public StorageType getStorageType() {
		return storageType;
	}

	public void setStorageType(StorageType storageType) {
		this.storageType = storageType;
	}

	public enum StorageType {
		S3, LOCAL, REMOTE;
	}

	@Override
	public String toString() {
		return "FileMeta{" + "fileId='" + fileId + '\'' + ", fileName='" + fileName + '\'' + ", fileKey='" + fileKey
				+ '\'' + ", storageType=" + storageType + '}';
	}
}
