package io.hyscale.ctl.deployer.services.exception;

import io.hyscale.ctl.commons.exception.HyscaleErrorCode;
/**
 * Error Codes for Deployer Service
 *
 */
public enum DeployerErrorCodes implements HyscaleErrorCode {
    ERROR_WHILE_CREATING_PATCH("Error while creating patch "),
    KUBE_CONFIG_NOT_FOUND("Cannot find kubeconfig at {}"),
    INVALID_KUBE_CONFIG("Kubeconfig {} seems to be invalid "),
    UNABLE_TO_READ_KUBE_CONFIG("Unable to read kubeconfig "),
    FAILED_TO_RETRIEVE_POD("Failed to retrieve pods"),
    FAILED_TO_TAIL_POD("Failed to tail pod logs"),
    RESOURCE_NOT_FOUND("Resource {} not found "),
    FAILED_TO_GET_RESOURCE("Failed to get resource {} "),
    FAILED_TO_CREATE_RESOURCE("Failed to create resource {}"),
    FAILED_TO_DELETE_RESOURCE("Failed to delete resource {}"),
    FAILED_TO_UPDATE_RESOURCE("Failed to update resource {}"),
    FAILED_TO_PATCH_RESOURCE("Failed to patch resource {}"),
    FAILED_TO_GET_SERVICE_ADDRESS("Failed to get service address of service {} "),
    MANIFEST_REQUIRED("Manifest required"),
    FAILED_TO_APPLY_MANIFEST("Failed to apply manifests"),
    FAILED_TO_READ_MANIFEST("Failed to read manifests"),
    SERVICE_REQUIRED("Service required"),
    APPLICATION_REQUIRED("Application name required"),
    FAILED_TO_GET_LOGS("Failed to get pod logs"),
    FAILED_TO_INITIALIZE_POD("Failed to initialize pod"),
    POD_FAILED_READINESS("Pod Readiness failed"),
    FAILED_TO_CREATE_POD("Failed to create pod"),
    OPERATION_NOT_SUPPORTED("Operation {} not supported for resource {}"),
    INVALID_STORAGE_CLASS_FOR_VOLUME("Storage class invalid in volume {}"),
	NO_STORAGE_CLASS_IN_K8S("No storage class defined in Kubernetes cluster");

    private String message;

    DeployerErrorCodes(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
