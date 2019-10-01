package io.hyscale.ctl.commons.utils;

import java.util.Map;
import java.util.stream.Collectors;

import io.hyscale.ctl.commons.models.ResourceLabelKey;

public class ResourceSelectorUtil {

	public static String getSelectorFromLabelMap(Map<ResourceLabelKey, String> label) {
		if (label == null || label.isEmpty()) {
			return null;
		}
		return label.entrySet().stream().map((entry) -> entry.getKey().getLabel() + "=" + entry.getValue())
				.collect(Collectors.joining(","));
	}

	public static String getSelector(String appName, String envName, String serviceName) {
		return getSelectorFromLabelMap(ResourceLabelBuilder.build(appName, envName, serviceName));
	}

	public static String getServiceSelector(String appName, String serviceName) {
		return getSelectorFromLabelMap(ResourceLabelBuilder.buildServiceLabel(appName, serviceName));
	}

	public static String getSelector(String appName, String envName) {
		return getSelectorFromLabelMap(ResourceLabelBuilder.build(appName, envName));
	}

	public static String getSelector(String appName) {
		return getSelectorFromLabelMap(ResourceLabelBuilder.build(appName));
	}

}
