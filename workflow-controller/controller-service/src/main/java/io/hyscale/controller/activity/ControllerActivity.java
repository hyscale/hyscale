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
package io.hyscale.controller.activity;

import io.hyscale.commons.models.Activity;

/**
 * Activities list for Workflow controller
 *
 */
public enum ControllerActivity implements Activity {
    DOCKERFILE_GENERATION("Dockerfile Generation"),
    DOCKERFILE_GENERATION_FAILED(" Dockerfile generation failed :: {}"),
    MANIFEST_GENERATION_FAILED(" Manifest generation failed :: {} "),
    CLEANING_UP_RESOURCES("Deleting stale resources"),
    CLEANING_UP_VOLUMES("Deleting stale pvc"),
    BUILD_AND_PUSH("Image build and Push"),
    STARTING_MANIFEST_GENERATION(" Manifest Generation "),
    STARTING_DEPLOYMENT(" Deployment "),
    STARTING_UNDEPLOYMENT(" Undeployment "),
    SERVICE_LOGS("Service Logs "),
    APP_NAME("APP: {}"),
    DEPLOYMENT_FAILED("DEPLOYMENT FAILED {} "),
    UNDEPLOYMENT_DONE(" Undeployment completed "),
    UNDEPLOYMENT_FAILED(" UNDEPLOYMENT FAILED {} "),
    SERVICE_NAME("SERVICE: {}"),
    SERVICE_URL("Service IP : {}"),
    CHECK_SERVICE_STATUS("Check service status for more information"),
    FAILED_TO_STREAM_SERVICE_LOGS("Unable to fetch service logs "),
    SERVICE_NOT_CREATED("Service is not created "),
    BUILD_LOGS("Build Logs : {}"),
    PUSH_LOGS("Push Logs : {}"),
    MANIFESTS_GENERATION_PATH("Manifests  path : {} "),
    DOCKERFILE_PATH("Dockerfile path : {}"),
    DEPLOY_LOGS_AT("Deploy Logs : {} "),
    CANNOT_PROCESS_SERVICE_SPEC("Service spec cannot be processed :: {} "),
    CANNOT_PROCESS_SERVICE_PROFILE("Service Profile cannot be processed:: {} "),
    NO_SERVICE_FOUND_FOR_PROFILE("Service spec not provided for service {}, their profiles are ignored"),
    APPLYING_PROFILE_FOR_SERVICE("Applying Profile {} for service {} "),
    PROFILE_NOT_FOUND("Profile {} not found for service {}"),
    ERROR_WHILE_READING("Error while reading {} due to {}"),
    ERROR_WHILE_FETCHING_STATUS("Error while fetching status"),
    NO_SERVICE_DEPLOYED("No Service Deployed"),
    CANNOT_FIND_FILE("Cannot find file {}"),
    INFORMATION(" Deployment info "),
    UNDEPLOYMENT_INFO(" Undeployment info"),
    ERROR("ERROR"),
    CAUSE("REASON :: {}"),
    UNEXPECTED_ERROR("Unexpected error occurred. For details refer to log file at {}"),
    INVALID_INPUT("Invalid input:: {}"),
    TOTAL_TIME("Total time : {}"),
    FAILED_TO_FETCH_DEPLOY_LOGS("Failed to fetch deploy logs {}"),
    INPUT_REPLICA_DETAIL("Enter replica index or name to continue..."),
    INVALID_INPUT_RETRY("Invalid input {} provided, please retry");

    private String message;

    ControllerActivity(String message) {
        this.message = message;
    }

    @Override
    public String getActivityMessage() {
        return message;
    }
}
