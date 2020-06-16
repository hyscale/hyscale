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
package io.hyscale.builder.services.exception;

import io.hyscale.commons.exception.HyscaleErrorCode;

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
    FAILED_TO_BUILD_AND_PUSH_IMAGE("Failed to build & push image"),
    DOCKERFILE_REQUIRED("Dockerfile required"),
    DOCKERFILE_NOT_FOUND("Dockerfile not found at {}"),
    MISSING_DOCKER_REGISTRY_CREDENTIALS("Cannot find {} credentials . Do 'docker login {}' to continue with the deployment");

    private String message;

    ImageBuilderErrorCodes(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
