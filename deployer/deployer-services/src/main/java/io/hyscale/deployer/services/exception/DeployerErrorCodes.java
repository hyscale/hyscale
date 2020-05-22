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
package io.hyscale.deployer.services.exception;

import io.hyscale.commons.exception.HyscaleErrorCode;

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
    REPLICA_DOES_NOT_EXIT("Cannot find replica \"{}\""),
    RESOURCE_NOT_FOUND("Resource not found {} "),
    FAILED_TO_GET_RESOURCE("Failed to get resource {} "),
    FAILED_TO_CREATE_RESOURCE("Failed to create resource {}"),
    FAILED_TO_DELETE_RESOURCE("Failed to delete resource {}"),
    FAILED_TO_UPDATE_RESOURCE("Failed to update resource {}"),
    FAILED_TO_PATCH_RESOURCE("Failed to patch resource {}"),
    FAILED_TO_GET_SERVICE_ADDRESS("Failed to get service address of service \"{}\" in namespace \"{}\""),
    MANIFEST_REQUIRED("Manifest required"),
    FAILED_TO_APPLY_MANIFEST("Failed to apply manifests"),
    FAILED_TO_READ_MANIFEST("Failed to read manifests"),
    SERVICE_REQUIRED("Service required"),
    APPLICATION_REQUIRED("Application name required"),
    FAILED_TO_GET_LOGS("Failed to get pod logs"),
    FAILED_TO_INITIALIZE_POD("Failed to initialize pod"),
    POD_FAILED_READINESS("Pod Readiness failed"),
    POD_SELECTOR_NOT_FOUND("Pod selector not found"),
    FAILED_TO_CREATE_POD("Failed to create pod"),
    OPERATION_NOT_SUPPORTED("Operation {} not supported for resource {}"),
    INVALID_STORAGE_CLASS_FOR_VOLUME("Storage class {} in your hspec is found to be invalid , allowed values from your cluster are {}"),
    NO_STORAGE_CLASS_IN_K8S("No storage class defined in your kubernetes cluster. Please contact your cluster administrator"),
    MISSING_DEFAULT_STORAGE_CLASS("Missing default storage class from the cluster so kindly define storage class in your hspec volumes [{}] , allowed values are : {} "),
    MISSING_UNIQUE_DEFAULT_STORAGE_CLASS("More than 1 default storage class found in cluster, either define single default storage class or specify a storage class in your hspec volumes [{}] , allowed values are : {} "),
    TIMEDOUT_WHILE_WAITING_FOR_SCALING("Timedout while waiting for {} to be scaled"),
    ERROR_WHILE_SCALING("Error while scaling : {}"),
    FAILED_TO_RETRIEVE_SERVICE_REPLICAS("Failed to retrieve service replicas"),
    SERVICE_NOT_DEPLOYED("{} is not deployed in namespace {} under app {} "),
    CANNOT_SCALE_NEGATIVE("Invalid value {} : must be greater than or equal to 0"),
    TIMEOUT_WHILE_WAITING_FOR_DEPLOYMENT("Timedout while waiting for deployment");

    private String message;

    DeployerErrorCodes(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
