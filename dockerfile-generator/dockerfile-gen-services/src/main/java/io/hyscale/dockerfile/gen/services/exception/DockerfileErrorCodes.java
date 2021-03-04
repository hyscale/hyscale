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
package io.hyscale.dockerfile.gen.services.exception;

import io.hyscale.commons.exception.HyscaleError;
import io.hyscale.commons.exception.HyscaleErrorGroup;

public enum DockerfileErrorCodes implements HyscaleError {
    FAILED_TO_GENERATE_DOCKERFILE("Failed to generate Dockerfile",HyscaleErrorGroup.DOCKER_FILE_GENERATION),
    FAILED_TO_COPY_ARTIFACT("Failed to copy artifacts",HyscaleErrorGroup.DOCKER_FILE_GENERATION),
    FAILED_TO_CREATE_SCRIPT("Failed to create script",HyscaleErrorGroup.DOCKER_FILE_GENERATION),
    FAILED_TO_PERSIST_DOCKERFILE("Failed to persist Dockerfile",HyscaleErrorGroup.DOCKER_FILE_GENERATION),
    BUILD_SPEC_REQUIRED("BuildSpec Required",HyscaleErrorGroup. DOCKER_FILE_GENERATION),
    DOCKERFILE_OR_BUILDSPEC_REQUIRED("Expected either Dockerfile or BuildSpec. Cannot accept both to define image spec",HyscaleErrorGroup.DOCKER_FILE_GENERATION),
    FAILED_TO_PROCESS_DOCKERFILE_GENERATION("Failed to process Dockerfile Generation",HyscaleErrorGroup.DOCKER_FILE_GENERATION),
    ARTIFACTS_FOUND_INVALID_IN_SERVICE_SPEC("Artifacts found to be invalid in the given service spec",HyscaleErrorGroup.DOCKER_FILE_GENERATION),
    CANNOT_RESOLVE_STACK_IMAGE("Cannot resolve stack image",HyscaleErrorGroup.DOCKER_FILE_GENERATION),
    SCRIPT_FILE_NOT_FOUND("Script file {} not found", HyscaleErrorGroup.DOCKER_FILE_GENERATION),
    INVALID_STACK_IMAGE("Stack image in build spec is invalid", HyscaleErrorGroup.DOCKER_FILE_GENERATION),
    ARTIFACTS_NOT_FOUND("Artifacts {} not found", HyscaleErrorGroup.DOCKER_FILE_GENERATION);

    private String message;
    private int code;

    DockerfileErrorCodes(String message) {
        this.message = message;
    }

    DockerfileErrorCodes(String message, HyscaleErrorGroup errorGroup) {
        this.message = message;
        this.code=errorGroup.getGroupCode();
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public int getCode() {
        return this.code;
    }

}
