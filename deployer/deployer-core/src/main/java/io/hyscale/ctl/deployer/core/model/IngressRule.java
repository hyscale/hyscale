package io.hyscale.ctl.deployer.core.model;

public class IngressRule {

	private int port;
	private String path;
	private String host;
	private String rewriteTarget;
	private boolean ssl;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getRewriteTarget() {
		return rewriteTarget;
	}

	public void setRewriteTarget(String rewriteTarget) {
		this.rewriteTarget = rewriteTarget;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		IngressRule that = (IngressRule) o;
		if (port != that.port)
			return false;
		if (ssl != that.ssl)
			return false;
		if (path != null ? !path.equals(that.path) : that.path != null)
			return false;
		if (host != null ? !host.equals(that.host) : that.host != null)
			return false;
		return rewriteTarget != null ? rewriteTarget.equals(that.rewriteTarget) : that.rewriteTarget == null;
	}

	@Override
	public int hashCode() {
		int result = path != null ? path.hashCode() : 0;
		result = 31 * result + port;
		result = 31 * result + (host != null ? host.hashCode() : 0);
		result = 31 * result + (rewriteTarget != null ? rewriteTarget.hashCode() : 0);
		result = 31 * result + (ssl ? 1 : 0);
		return result;
	}
}
