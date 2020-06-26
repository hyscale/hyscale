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
package io.hyscale.controller.exception;

import io.hyscale.commons.exception.HyscaleError;
import io.hyscale.commons.exception.HyscaleErrorGroup;

/**
 * Error Codes for Workflow Controller
 */
public enum ControllerErrorCodes implements HyscaleError {
    INVALID_COMMAND("Invalid command {}"),
    MANIFEST_REQUIRED("Manifest required"),
    DOCKER_CONFIG_NOT_FOUND("Cannot find config.json at {}"),
    KUBE_CONFIG_NOT_FOUND("Cannot find kubeconfig(config) at {}"),
    KUBE_CONFIG_PATH_EMPTY("Kube config path is empty"),
    SERVICE_SPEC_PROCESSING_FAILED("Failed to process service spec "),
    UNEXPECTED_ERROR("Unexpected error occurred"),
    UNDEPLOYMENT_FAILED("Failed to Undeploy"),
    UNIQUE_PROFILE_REQUIRED("Service {} cannot have multiple profiles"),
    INVALID_REPLICA_SELECTED_REACHED_MAX_RETRIES("Invalid input provided. Reached maximum retries. Please try again"),
    PROFILE_NOT_PROVIDED_FOR_SERVICES("Profile not found for services {}"),
    SERVICES_NOT_PROVIDED_FOR_PROFILE("Services {} mentioned in profile are not found"),
    ERROR_WHILE_PROCESSING_PROFILE("Error while looking for profile. {}"),
    INPUT_VALIDATION_FAILED("Input validation failed. Error messages {}"),
    PROFILE_VALIDATION_FAILED("Profile validation failed"),
    PROFILE_NAMES_MISMATCHED_WITH_FILES("Profile name mismatched with environment in {}"),
    FAILED_TO_SCALE_SERVICE("Failed to scale {} of application {} in {} namespace"),
    MANIFEST_VALIDATION_FAILED("Manifest validation failed.",HyscaleErrorGroup.UPFRONT_VALIDATION);

    private String message;
    private int code;

    ControllerErrorCodes(String message) {
        this.message = message;
    }

    ControllerErrorCodes(String message,HyscaleErrorGroup errorGroup){
        this.message=message;
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
