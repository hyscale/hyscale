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

import java.util.Objects;

public class K8sBasicAuth implements K8sAuthorisation {
	private String userName;
	private String password;
	private String masterURL;
	private String token;
	private String caCert;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMasterURL() {
		return masterURL;
	}

	public void setMasterURL(String masterURL) {
		this.masterURL = masterURL;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getCaCert() {
		return caCert;
	}

	public void setCaCert(String caCert) {
		this.caCert = caCert;
	}

	@Override
	public K8sAuthType getK8sAuthType() {
		return K8sAuthType.BASIC_AUTH;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		K8sBasicAuth that = (K8sBasicAuth) o;
		return Objects.equals(userName, that.userName) &&
				Objects.equals(password, that.password) &&
				Objects.equals(masterURL, that.masterURL) &&
				Objects.equals(token, that.token) &&
				Objects.equals(caCert, that.caCert);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userName, password, masterURL, token, caCert);
	}
}
