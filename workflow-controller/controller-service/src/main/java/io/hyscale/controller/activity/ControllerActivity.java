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
    SERVICE_LOGS("Service Logs from {}"),
    APP_NAME("APP: {}"),
    UNDEPLOYMENT_DONE(" Undeployment completed "),
    UNDEPLOYMENT_FAILED(" UNDEPLOYMENT FAILED {} "),
    SERVICE_NAME("SERVICE: {}"),
    SERVICE_IP("Service IP : {}"),
    SERVICE_URL("Service URL : {}"),
    CHECK_SERVICE_STATUS("Check service status for more information"),
    FAILED_TO_STREAM_SERVICE_LOGS("Unable to fetch service logs "),
    REPLICA_DOES_NOT_EXIT("Cannot find replica \"{}\". Available replicas {}"),
    SERVICE_NOT_CREATED("Service is not created "),
    BUILD_LOGS("Build Logs : {}"),
    PUSH_LOGS("Push Logs : {}"),
    MANIFESTS_GENERATION_PATH("Manifests  path : {} "),
    DOCKERFILE_PATH("Dockerfile path : {}"),
    DEPLOY_LOGS_AT("Deploy Logs : {} "),
    NO_SERVICE_FOUND_FOR_PROFILE("Service Spec not found for services {} mentioned in profiles."),
    LOOKING_FOR_PROFILE("Looking for profile {}"),
    APPLYING_PROFILE_FOR_SERVICE("Applying profile \"{}\" for service \"{}\""),
    ERROR_WHILE_PROCESSING_PROFILE("Error while looking for profile \"{}\". {}"),
    MULIPLE_PROFILES_FOUND("Multiple profiles found for services {}. Only one profile is allowed per deployment"),
    ERROR_WHILE_READING("Error while reading {} due to {}"),
    ERROR_WHILE_FETCHING_STATUS("Error while fetching status"),
    ERROR_WHILE_FETCHING_DEPLOYMENTS("Error while fetching deployments from cluster"),
    NO_SERVICE_DEPLOYED("No Service Deployed"),
    NO_DEPLOYMENTS("No deployments found on the cluster"),
    CANNOT_FIND_FILE("Cannot find file {}"),
    INFORMATION(" Deployment info "),
    UNDEPLOYMENT_INFO(" Undeployment info"),
    ERROR("ERROR"),
    CAUSE("REASON :: {}"),
    UNEXPECTED_ERROR("Unexpected error occurred. For details refer to log file at {}"),
    INVALID_INPUT("Invalid input:: {}"),
    TOTAL_TIME("Total time : {}"),
    INPUT_REPLICA_DETAIL("Enter replica index or name to continue..."),
    INVALID_INPUT_RETRY("Input {} is invalid, please retry"),
    EMPTY_FILE_PATH("Empty file path given"),
    EMPTY_FILE_FOUND("Empty file {} cannot be processed."),
    INVALID_FILE_INPUT("Given input {} is not a file.Expecting file input."),
    DIRECTORY_INPUT_FOUND("Found directory {} instead of file"),
    WAITING_FOR_SERVICE_STATUS("It might take some time as it runs diagnosis for services"),
    TROUBLESHOOT("{}"), 
    APPLICATION_DETAILS("Applications"),
    SUCCESSFULLY_SCALED("Successfully  {} {}");

    private String message;

    ControllerActivity(String message) {
        this.message = message;
    }

    @Override
    public String getActivityMessage() {
        return message;
    }
    
}
