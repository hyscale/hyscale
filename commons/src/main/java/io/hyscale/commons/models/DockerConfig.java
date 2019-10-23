package io.hyscale.commons.models;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerConfig {

	private Map<String, Auth> auths = new HashMap<>();

	public Map<String, Auth> getAuths() {
		return auths;
	}

	public void setAuths(Map<String, Auth> auths) {
		this.auths = auths;
	}
}
