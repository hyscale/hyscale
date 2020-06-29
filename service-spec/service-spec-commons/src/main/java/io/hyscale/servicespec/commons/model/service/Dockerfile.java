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

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dockerfile {

	private String path;
	private String dockerfilePath;
	private String target;
	private Map<String, String> args;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDockerfilePath() {
		return dockerfilePath;
	}

	public void setDockerfilePath(String dockerfilePath) {
		this.dockerfilePath = dockerfilePath;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public Map<String, String> getArgs() {
		return args;
	}

	public void setArgs(Map<String, String> args) {
		this.args = args;
	}

	@Override
	public int hashCode() {
		return Objects.hash(args, dockerfilePath, path, target);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dockerfile other = (Dockerfile) obj;
		return Objects.equals(args, other.args) && Objects.equals(dockerfilePath, other.dockerfilePath)
				&& Objects.equals(path, other.path) && Objects.equals(target, other.target);
	}

    @Override
    public String toString() {
        return "Dockerfile [path=" + path + ", dockerfilePath=" + dockerfilePath + ", target=" + target + ", args="
                + args + "]";
    }
}
