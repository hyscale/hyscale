package io.hyscale.commons.utils;

import java.util.Map;

import io.hyscale.commons.models.ResourceLabelKey;

public class ResourceLabelUtil {

	public static String getServiceName(Map<String, String> labels) {
		if (labels == null) {
			return null;
		}
		return labels.get(ResourceLabelKey.SERVICE_NAME.getLabel());
	}

	public static String getAppName(Map<String, String> labels) {
		if (labels == null) {
			return null;
		}
		return labels.get(ResourceLabelKey.APP_NAME.getLabel());
	}
	
	public static String getEnvName(Map<String, String> labels) {
		if (labels == null) {
			return null;
		}
		return labels.get(ResourceLabelKey.ENV_NAME.getLabel());
	}

}
