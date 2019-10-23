package io.hyscale.commons.models;

public enum AnnotationKey {

	PVC_TEMPLATE_STORAGE_CLASS("volume.beta.kubernetes.io/storage-class"),
	K8S_HYSCALE_LAST_APPLIED_CONFIGURATION("hyscale.io/last-applied-configuration"),
	K8S_DEFAULT_STORAGE_CLASS("storageclass.beta.kubernetes.io/is-default-class"),
	K8S_STORAGE_CLASS("volume.beta.kubernetes.io/storage-class"),
	K8S_DEPLOYMENT_REVISION("deployment.kubernetes.io/revision"),
	K8S_STS_POD_NAME("statefulset.kubernetes.io/pod-name"),
	HYSCALE_SERVICE_SPEC("hyscale.io/service-spec"),
	DEFAULT_STORAGE_CLASS("storageclass.kubernetes.io/is-default-class"),
	DEFAULT_BETA_STORAGE_CLASS("storageclass.beta.kubernetes.io/is-default-class");

	private String annotation;

	AnnotationKey(String annotationKey) {
		this.annotation = annotationKey;
	}

	public String getAnnotation() {
		return annotation;
	}
}
