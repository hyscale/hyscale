package io.hyscale.servicespec.commons.model.profile;

import java.util.List;
import java.util.Map;

/**
 * Service profile model
 *
 */
public class ProfileSpecification {

	private String environment;
	private String overrides;
	private Integer replicas;
	private Map<String, String> props;
	private Map<String, String> secrets;
	private List<Volume> volumes;

	public List<Volume> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<Volume> volumes) {
		this.volumes = volumes;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public String getOverrides() {
		return overrides;
	}

	public void setOverrides(String overrides) {
		this.overrides = overrides;
	}

	public Integer getReplicas() {
		return replicas;
	}

	public void setReplicas(Integer replicas) {
		this.replicas = replicas;
	}

	public Map<String, String> getProps() {
		return props;
	}

	public void setProps(Map<String, String> props) {
		this.props = props;
	}

	public Map<String, String> getSecrets() {
		return secrets;
	}

	public void setSecrets(Map<String, String> secrets) {
		this.secrets = secrets;
	}
}
