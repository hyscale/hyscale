package io.hyscale.servicespec.commons.model.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import io.hyscale.servicespec.annotations.StrategicMergePatch;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceSpecification implements Serializable {
	private String name;
	private List<String> depends;
	private List<Port> ports;
	private Map<String, String> props;
	private Map<String, String> secrets;

	@StrategicMergePatch(key = "name")
	private List<Volume> volumes;
	private Integer replicas;
	private String propsVolumePath;
	private String secretsVolumePath;

	@StrategicMergePatch(key = "name")
	private List<Agent> agents;
	private Image image;
	private boolean external;

	public boolean isExternal() {
		return external;
	}

	public void setExternal(boolean external) {
		this.external = external;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getDepends() {
		return depends;
	}

	public void setDepends(List<String> depends) {
		this.depends = depends;
	}

	public List<Port> getPorts() {
		return ports;
	}

	public void setPorts(List<Port> ports) {
		this.ports = ports;
	}

	public List<Volume> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<Volume> volumes) {
		this.volumes = volumes;
	}

	public Integer getReplicas() {
		return replicas;
	}

	public void setReplicas(Integer replicas) {
		this.replicas = replicas;
	}

	public String getPropsVolumePath() {
		return propsVolumePath;
	}

	public void setPropsVolumePath(String propsVolumePath) {
		this.propsVolumePath = propsVolumePath;
	}

	public String getSecretsVolumePath() {
		return secretsVolumePath;
	}

	public void setSecretsVolumePath(String secretsVolumePath) {
		this.secretsVolumePath = secretsVolumePath;
	}

	public List<Agent> getAgents() {
		return agents;
	}

	public void setAgents(List<Agent> agents) {
		this.agents = agents;
	}
}
