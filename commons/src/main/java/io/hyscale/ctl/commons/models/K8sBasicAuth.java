package io.hyscale.ctl.commons.models;

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
}
