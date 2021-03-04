/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.servicespec.commons.model.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Agent {

	private String name;
	private String image;
	private Map<String,String> props;
	private Secrets secrets;
	private List<AgentVolume> volumes;
	private String propsVolumePath;
	private String secretsVolumePath;
	private List<Port> ports;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public List<AgentVolume> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<AgentVolume> volumes) {
		this.volumes = volumes;
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

	public Secrets getSecrets() {
		return secrets;
	}

	public void setSecrets(Secrets secrets) {
		this.secrets = secrets;
	}

	public Map<String, String> getProps() {
		return props;
	}

	public void setProps(Map<String, String> props) {
		this.props = props;
	}

	public List<Port> getPorts() {
		return ports;
	}

	public void setPorts(List<Port> ports) {
		this.ports = ports;
	}
}
