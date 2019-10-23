package io.hyscale.commons.models;

import java.io.Reader;

public class K8sConfigReaderAuth implements K8sAuthorisation {

	private Reader k8sConfigReader;

	public Reader getK8sConfigReader() {
		return k8sConfigReader;
	}

	public void setK8sConfigReader(Reader k8sConfigReader) {
		this.k8sConfigReader = k8sConfigReader;
	}

	@Override
	public K8sAuthType getK8sAuthType() {
		return K8sAuthType.KUBE_CONFIG_READER;
	}

}