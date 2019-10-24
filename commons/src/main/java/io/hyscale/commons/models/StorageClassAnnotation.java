package io.hyscale.commons.models;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StorageClassAnnotation {

	private static List<String> defaultAnnotations = Arrays.asList(
			AnnotationKey.DEFAULT_STORAGE_CLASS.getAnnotation(),
			AnnotationKey.DEFAULT_BETA_STORAGE_CLASS.getAnnotation());

	public static String getDefaultAnnotaionValue(Map<String, String> annotations) {
		if (annotations == null) {
			return null;
		}
		for (Entry<String, String> entry : annotations.entrySet()) {
			if (defaultAnnotations.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}
}
