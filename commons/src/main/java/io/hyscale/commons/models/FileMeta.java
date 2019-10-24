/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.commons.models;

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
