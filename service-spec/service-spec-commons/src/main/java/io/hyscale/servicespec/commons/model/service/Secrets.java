package io.hyscale.servicespec.commons.model.service;

import java.util.Map;
import java.util.Set;

public class Secrets {

	private Set<String> secretKeys;
	private Map<String, String> secretsMap;

	public Set<String> getSecretKeys() {
		return secretKeys;
	}

	public void setSecretKeys(Set<String> secretKeys) {
		this.secretKeys = secretKeys;
	}

	public Map<String, String> getSecretsMap() {
		return secretsMap;
	}

	public void setSecretsMap(Map<String, String> secretsMap) {
		this.secretsMap = secretsMap;
	}
}
