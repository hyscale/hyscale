package io.hyscale.ctl.builder.services.exception;

import io.hyscale.ctl.commons.exception.HyscaleErrorCode;

public enum ImageBuilderErrorCodes implements HyscaleErrorCode {
    DOCKER_NOT_INSTALLED("Docker is not installed"),
    FAILED_TO_TAG_IMAGE("Failed to tag image "),
    FAILED_TO_BUILD_IMAGE("Failed to build image"),
    FAILED_TO_PUSH_IMAGE("Failed to push image"),
    FAILED_TO_LOGIN("Failed to authenticate with the docker registry"),
    FAILED_TO_PULL_IMAGE("Failed to pull image {}"),
    FIELDS_MISSING("Found {} missing"),
    DOCKER_DAEMON_NOT_RUNNING("Docker daemon is not running"),
    CANNOT_RESOLVE_IMAGE_NAME("Cannot resolve image name"),
    FAILED_TO_BUILD_AND_PUSH_IMAGE("Failed to build & push image");

    private String message;

    ImageBuilderErrorCodes(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
