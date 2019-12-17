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

import io.hyscale.commons.exception.HyscaleErrorCode;

public enum DockerfileErrorCodes implements HyscaleErrorCode {
    CONTEXT_REQUIRED("Dockerfile Generation Context required"),
    FAILED_TO_GENERATE_DOCKERFILE("Failed to generate Dockerfile"),
    FAILED_TO_COPY_ARTIFACT("Failed to copy artifacts"),
    FAILED_TO_CREATE_SCRIPT("Failed to create script"),
    FAILED_TO_PERSIST_DOCKERFILE("Failed to persist Dockerfile"),
    BUILD_SPEC_REQUIRED("BuildSpec Required"),
    DOCKERFILE_OR_BUILDSPEC_REQUIRED("Either Dockerfile or BuildSpec is required to build an image"),
    FAILED_TO_PROCESS_DOCKERFILE_GENERATION("Failed to process Dockerfile Generation"),
    ARTIFACTS_FOUND_INVALID_IN_SERVICE_SPEC("Artifacts found to be invalid in the given service spec"),
    CANNOT_RESOLVE_STACK_IMAGE("Cannot resolve stack image");

    private String message;

    DockerfileErrorCodes(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
