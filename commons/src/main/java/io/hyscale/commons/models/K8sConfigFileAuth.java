package io.hyscale.commons.models;

import java.io.File;

public class K8sConfigFileAuth implements K8sAuthorisation {

	private File k8sConfigFile;

	public File getK8sConfigFile() {
		return k8sConfigFile;
	}

	public void setK8sConfigFile(File k8sConfigFile) {
		this.k8sConfigFile = k8sConfigFile;
	}

	@Override
	public K8sAuthType getK8sAuthType() {
		return K8sAuthType.KUBE_CONFIG_FILE;
	}

}