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
