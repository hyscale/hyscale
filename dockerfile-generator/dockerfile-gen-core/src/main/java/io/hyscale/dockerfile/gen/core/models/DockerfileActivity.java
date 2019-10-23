package io.hyscale.dockerfile.gen.core.models;

import io.hyscale.commons.models.Activity;

public enum DockerfileActivity implements Activity {
	DOCKERFILE_GENERATION("Generating Dockerfile "),
	SUPPORT_FILES_COPY("Copying support files ");

	private String message;

	private DockerfileActivity(String message) {
		this.message = message;
	}

	@Override
	public String getActivityMessage() {
		return this.message;
	}

}
