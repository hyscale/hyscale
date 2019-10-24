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
    FAILED_TO_STREAM_SERVICE_LOGS("Cannot to fetch service logs "),
    SERVICE_NOT_CREATED("Service is not created "),
    BUILD_LOGS("Build Logs : {}"),
    PUSH_LOGS("Push Logs : {}"),
    MANIFESTS_GENERATION_PATH("Manifests  path : {} "),
    DOCKERFILE_PATH("Dockerfile path : {}"),
    DEPLOY_LOGS_AT("Deploy Logs : {} "),
    CANNOT_PROCESS_SERVICE_SPEC("Service spec cannot be processed :: {} "),
    ERROR_WHILE_READING("Error while reading {} due to {}"),
    ERROR_WHILE_FETCHING_STATUS("Error while fetching status {}"),
    NO_SERVICE_DEPLOYED("No Service Deployed"),
    CANNOT_FIND_FILE("Cannot find file {}"),
    INFORMATION(" Deployment info "),
    ERROR("ERROR"),
    CAUSE("REASON :: {}"),
    UNEXPECTED_ERROR("Unexpected error occurred. For details refer to log file at {}"),
    INVALID_INPUT("Invalid input:: {}"),
    TOTAL_TIME("Total time : {}"),
    FAILED_TO_FETCH_DEPLOY_LOGS("Failed to fetch deploy logs {}");

    private String message;

    ControllerActivity(String message) {
        this.message = message;
    }

    @Override
    public String getActivityMessage() {
        return message;
    }
}
