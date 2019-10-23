package io.hyscale.builder.core.models;

import io.hyscale.commons.models.Activity;

public enum ImageBuilderActivity implements Activity {

	IMAGE_BUILD_PUSH("Image build and push "),
	IMAGE_BUILD_STARTED("Building image "),
	BUILD_LOGS("Build Logs "),
	IMAGE_TAG("Tagging image "),
	IMAGE_PULL("Pulling image "),
	IMAGE_PUSH("Pushing image "),
	IMAGE_PUSH_LOG("Push logs "),
	LOGIN("Log in to registry "),
	DOCKER_DAEMON_NOT_RUNNING("Docker daemon not running");

	private String message;

	ImageBuilderActivity(String message) {
		this.message = message;
	}

	@Override
	public String getActivityMessage() {
		return message;
	}
}
