package io.hyscale.ctl.commons.models;

import io.kubernetes.client.models.V1ObjectMeta;

public class KubernetesResource {

	private Object resource;
	private V1ObjectMeta v1ObjectMeta;
	private String kind;

	public Object getResource() {
		return resource;
	}

	public void setResource(Object resource) {
		this.resource = resource;
	}

	public V1ObjectMeta getV1ObjectMeta() {
		return v1ObjectMeta;
	}

	public void setV1ObjectMeta(V1ObjectMeta v1ObjectMeta) {
		this.v1ObjectMeta = v1ObjectMeta;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

}
