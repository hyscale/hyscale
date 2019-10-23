package io.hyscale.generator.services.utils;

import java.util.HashMap;
import java.util.Map;

import io.hyscale.commons.models.AnnotationKey;

public class AnnotationBuilder {

	private static final long MAX_ANNOTATION_VALUE_SIZE = 64000;

	// TODO CHECK THIS LIMIT
	private static final int MAX_ANNOTATION_KEY_SIZE = 1024;

	public static Map<AnnotationKey, String> volumeClaimTemplate(String storageClass) {
		Map<AnnotationKey, String> annotations = new HashMap<AnnotationKey, String>();
		annotations.put(AnnotationKey.PVC_TEMPLATE_STORAGE_CLASS, normalize(storageClass));
		return annotations;
	}

	public static Map<AnnotationKey, String> serviceSpec(String serviceSpec) {
		Map<AnnotationKey, String> annotations = new HashMap<AnnotationKey, String>();
		annotations.put(AnnotationKey.HYSCALE_SERVICE_SPEC,serviceSpec);
		return annotations;
	}

	public static String normalize(String name) {
		String normalized = name.replaceAll("[\\.]+", "-").replaceAll("[ ]+", "-").replaceAll("[^a-zA-Z0-9-_]", "");
		return normalized.substring(0, Integer.min(MAX_ANNOTATION_KEY_SIZE - 1, name.length()));
	}
}
