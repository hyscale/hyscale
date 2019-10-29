package io.hyscale.commons.utils;

import java.util.Map;

import io.hyscale.commons.models.ResourceLabelKey;

public class ResourceLabelUtil {

	public static String getServiceFromLabel(Map<String, String> labels) {
		if (labels == null) {
			return null;
		}
		return labels.get(ResourceLabelKey.SERVICE_NAME.getLabel());
	}

	public static String getAppFromLabel(Map<String, String> labels) {
		if (labels == null) {
			return null;
		}
		return labels.get(ResourceLabelKey.APP_NAME.getLabel());
	}
	
	public static String getEnvFromLabel(Map<String, String> labels) {
		if (labels == null) {
			return null;
		}
		return labels.get(ResourceLabelKey.ENV_NAME.getLabel());
	}

}
