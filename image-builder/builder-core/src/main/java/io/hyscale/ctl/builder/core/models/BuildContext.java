package io.hyscale.ctl.builder.core.models;

import java.util.Map;

import io.hyscale.ctl.commons.models.DockerfileEntity;
import io.hyscale.ctl.commons.models.ImageRegistry;

public class BuildContext {

	private DockerfileEntity dockerfileEntity;
	private DockerImage dockerImage;
	private ImageRegistry imageRegistry;
	private String serviceName;
	private String appName;
	private String version;
	private Map<String, String> buildArgs;
	private boolean verbose;
	private boolean tail;
	private boolean stackAsServiceImage;
	private String imageShaSum;
	private String buildLogs;
	private String pushLogs;

	public DockerfileEntity getDockerfileEntity() {
		return dockerfileEntity;
	}

	public void setDockerfileEntity(DockerfileEntity dockerfileEntity) {
		this.dockerfileEntity = dockerfileEntity;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public DockerImage getDockerImage() {
		return dockerImage;
	}

	public void setDockerImage(DockerImage dockerImage) {
		this.dockerImage = dockerImage;
	}

	public ImageRegistry getImageRegistry() {
		return imageRegistry;
	}

	public void setImageRegistry(ImageRegistry imageRegistry) {
		this.imageRegistry = imageRegistry;
	}

	public Map<String, String> getBuildArgs() {
		return buildArgs;
	}

	public void setBuildArgs(Map<String, String> buildArgs) {
		this.buildArgs = buildArgs;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean isTail() {
		return tail;
	}

	public void setTail(boolean tail) {
		this.tail = tail;
	}

	public boolean isStackAsServiceImage() {
		return stackAsServiceImage;
	}

	public void setStackAsServiceImage(boolean stackAsServiceImage) {
		this.stackAsServiceImage = stackAsServiceImage;
	}

	public String getImageShaSum() {
		return imageShaSum;
	}

	public void setImageShaSum(String imageShaSum) {
		this.imageShaSum = imageShaSum;
	}

	public String getBuildLogs() {
		return buildLogs;
	}

	public void setBuildLogs(String buildLogs) {
		this.buildLogs = buildLogs;
	}

	public String getPushLogs() {
		return pushLogs;
	}

	public void setPushLogs(String pushLogs) {
		this.pushLogs = pushLogs;
	}
}
