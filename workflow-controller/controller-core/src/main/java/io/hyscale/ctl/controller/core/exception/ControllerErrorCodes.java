package io.hyscale.ctl.controller.core.exception;

import io.hyscale.ctl.commons.exception.HyscaleErrorCode;

/**
 * Error Codes for Workflow Controller
 *
 */
public enum ControllerErrorCodes implements HyscaleErrorCode {
    INVALID_COMMAND("Invalid command {}"),
    CANNOT_FIND_SERVICE_SPEC("Cannot find service spec {} "),
    SERVICE_SPEC_REQUIRED("Service spec required"),
    MANIFEST_REQUIRED("Manifest required"),
    DOCKER_CONFIG_NOT_FOUND("Cannot find config.json at {}"),
    KUBE_CONFIG_NOT_FOUND("Cannot find kubeconfig(config) at {}"),
    SERVICE_SPEC_PROCESSING_FAILED("Failed to process service spec "),
    INVALID_PORTS_FOUND("Ports found to be invalid in service spec"),
    INVALID_VOLUMES_FOUND("Volumes found to be invalid in service spec"),
    UNDEPLOYMENT_FAILED("Failed to Undeploy");

    private String message;

    ControllerErrorCodes(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
