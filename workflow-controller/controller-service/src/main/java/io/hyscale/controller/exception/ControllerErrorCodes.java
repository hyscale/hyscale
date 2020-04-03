/**
 * Copyright 2019 Pramati Prism, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.controller.exception;

import io.hyscale.commons.exception.HyscaleErrorCode;
import io.hyscale.commons.models.Activity;

/**
 * Error Codes for Workflow Controller
 */
public enum ControllerErrorCodes implements HyscaleErrorCode {
    INVALID_COMMAND("Invalid command {}"),
    CANNOT_FIND_SERVICE_SPEC("Cannot find service spec {} "),
    APP_NAME_REQUIRED("Application name required"),
    MANIFEST_REQUIRED("Manifest required"),
    DOCKER_CONFIG_NOT_FOUND("Cannot find config.json at {}"),
    KUBE_CONFIG_NOT_FOUND("Cannot find kubeconfig(config) at {}"),
    KUBE_CONFIG_PATH_EMPTY("Kube config path is empty"),
    SERVICE_SPEC_PROCESSING_FAILED("Failed to process service spec "),
    INVALID_PORTS_FOUND("Ports found to be invalid in service spec"),
    INVALID_VOLUMES_FOUND("Volumes found to be invalid in service spec"),
    UNEXPECTED_ERROR("Unexpected error occurred"),
    UNDEPLOYMENT_FAILED("Failed to Undeploy"),
    UNIQUE_PROFILE_REQUIRED("Service {} cannot have multiple profiles"),
    INVALID_REPLICA_SELECTED_REACHED_MAX_RETRIES("Invalid input provided . Reached maximum retries. Please try again");

    private String message;

    ControllerErrorCodes(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
