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
package io.hyscale.commons.models;

import java.io.File;
import java.util.Objects;

public class K8sConfigFileAuth implements K8sAuthorisation {

	private File k8sConfigFile;

	public File getK8sConfigFile() {
		return k8sConfigFile;
	}

	public void setK8sConfigFile(File k8sConfigFile) {
		this.k8sConfigFile = k8sConfigFile;
	}

	@Override
	public K8sAuthType getK8sAuthType() {
		return K8sAuthType.KUBE_CONFIG_FILE;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		K8sConfigFileAuth that = (K8sConfigFileAuth) o;
		return k8sConfigFile.equals(that.k8sConfigFile);
	}

	@Override
	public int hashCode() {
		return Objects.hash(k8sConfigFile);
	}
}