package io.hyscale.ctl.deployer.core.model;

import org.apache.commons.lang3.StringUtils;

public enum ResourceKind {

	POD("Pod"),
	STATEFUL_SET("StatefulSet", 1),
	DEPLOYMENT("Deployment", 1),
	CONFIG_MAP("ConfigMap", 0),
	REPLICA_SET("ReplicaSet"),
	SECRET("Secret", 0),
	SERVICE("Service", 0),
	NAMESPACE("Namespace"),
	STORAGE_CLASS("StorageClass"),
	PERSISTENT_VOLUME_CLAIM("PersistentVolumeClaim", 2);

	private String kind;

	/**
	 *  Deletion and creation order
	 */
	private int weight = 0;

	ResourceKind(String kind) {
		this.kind = kind;
	}

	ResourceKind(String kind, int weight) {
		this.kind = kind;
		this.weight = weight;
	}

	public String getKind() {
		return this.kind;
	}

	public int getWeight() {
		return this.weight;
	}

	public static ResourceKind fromString(String kind) {
		if (StringUtils.isBlank(kind)) {
			return null;
		}
		for (ResourceKind resourceKind : ResourceKind.values()) {
			if (resourceKind.getKind().equalsIgnoreCase(kind)) {
				return resourceKind;
			}
		}
		return null;
	}
}
