package io.hyscale.ctl.commons.models;

import java.util.HashMap;
import java.util.Map;

public class ManifestContext {

	private String appName;
	private String envName;
	private String namespace;
	private ImageRegistry imageRegistry;
	private Map<String, Object> generationAttributes;

	public ManifestContext() {
		this.generationAttributes = new HashMap<>();
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public ImageRegistry getImageRegistry() {
		return imageRegistry;
	}

	public void setImageRegistry(ImageRegistry imageRegistry) {
		this.imageRegistry = imageRegistry;
	}

	public String getEnvName() {
		return envName;
	}

	public void setEnvName(String envName) {
		this.envName = envName;
	}

	public Object getGenerationAttribute(String key) {
		return generationAttributes.get(key);
	}

	public void addGenerationAttribute(String key, Object value) {
		generationAttributes.put(key, value);
	}

}
