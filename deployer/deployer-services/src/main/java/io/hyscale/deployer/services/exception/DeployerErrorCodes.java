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

import io.hyscale.commons.exception.HyscaleError;
import io.hyscale.commons.exception.HyscaleErrorGroup;

/**
 * Error Codes for Deployer Service
 *
 */
public enum DeployerErrorCodes implements HyscaleError {
    ERROR_WHILE_CREATING_PATCH("Error while creating patch "),
    KUBE_CONFIG_NOT_FOUND("Cannot find kubeconfig at {}",HyscaleErrorGroup.GET_API_CLIENT),
    INVALID_KUBE_CONFIG("Kubeconfig {} seems to be invalid ",HyscaleErrorGroup.GET_API_CLIENT),
    UNABLE_TO_READ_KUBE_CONFIG("Unable to read kubeconfig ",HyscaleErrorGroup.GET_API_CLIENT),
    FAILED_TO_RETRIEVE_POD("Failed to retrieve pods",HyscaleErrorGroup.WAIT_FOR_DEPLOYMENT),
    FAILED_TO_TAIL_POD("Failed to tail pod logs",HyscaleErrorGroup.GET_LOGS),
    REPLICA_DOES_NOT_EXIT("Cannot find replica \"{}\"",HyscaleErrorGroup.GET_LOGS),
    RESOURCE_NOT_FOUND("Resource not found {} ",HyscaleErrorGroup.DEPLOYER_APPLY),
    FAILED_TO_GET_RESOURCE("Failed to get resource {} ",HyscaleErrorGroup.DEPLOYER_APPLY),
    FAILED_TO_CREATE_RESOURCE("Failed to create resource {}. Message: {}",HyscaleErrorGroup.DEPLOYER_APPLY),
    FAILED_TO_DELETE_RESOURCE("Failed to delete resource {}"),
    FAILED_TO_UPDATE_RESOURCE("Failed to update resource {}"),
    FAILED_TO_PATCH_RESOURCE("Failed to patch resource {}"),
    FAILED_TO_GET_SERVICE_ADDRESS("Failed to get service address",HyscaleErrorGroup.GET_SERVICE_IP),
    MANIFEST_REQUIRED("Manifest required",HyscaleErrorGroup.DEPLOYER_APPLY),
    FAILED_TO_APPLY_MANIFEST("Failed to apply manifests,Please refer logs for more details",HyscaleErrorGroup.DEPLOYER_APPLY),
    FAILED_TO_READ_MANIFEST("Failed to read manifests"),
    SERVICE_REQUIRED("Service required"),
    APPLICATION_REQUIRED("Application name required"),
    FAILED_TO_GET_LOGS("Failed to get pod logs",HyscaleErrorGroup.GET_LOGS),
    FAILED_TO_INITIALIZE_POD("Failed to initialize pod",HyscaleErrorGroup.WAIT_FOR_DEPLOYMENT),
    POD_FAILED_READINESS("Pod Readiness failed",HyscaleErrorGroup.WAIT_FOR_DEPLOYMENT),
    POD_SELECTOR_NOT_FOUND("Pod selector not found"),
    FAILED_TO_CREATE_POD("Failed to create pod",HyscaleErrorGroup.WAIT_FOR_DEPLOYMENT),
    OPERATION_NOT_SUPPORTED("Operation {} not supported for resource {}"),
    INVALID_STORAGE_CLASS_FOR_VOLUME("Storage class {} in your hspec is found to be invalid , allowed values from your cluster are {}"),
    NO_STORAGE_CLASS_IN_K8S("No storage class defined in your kubernetes cluster. Please contact your cluster administrator",HyscaleErrorGroup.UPFRONT_VALIDATION),
    MISSING_DEFAULT_STORAGE_CLASS("Missing default storage class from the cluster so kindly define storage class in your hspec volumes [{}] , allowed values are : {} "),
    MISSING_UNIQUE_DEFAULT_STORAGE_CLASS("More than 1 default storage class found in cluster, either define single default storage class or specify a storage class in your hspec volumes [{}] , allowed values are : {} "),
    TIMEDOUT_WHILE_WAITING_FOR_SCALING("Timedout while waiting for {} to be scaled"),
    ERROR_WHILE_SCALING("Error while scaling : {}"),
    FAILED_TO_RETRIEVE_SERVICE_REPLICAS("Failed to retrieve service replicas",HyscaleErrorGroup.WAIT_FOR_DEPLOYMENT),
    SERVICE_NOT_DEPLOYED("{} is not deployed in namespace {} under app {} "),
    CANNOT_SCALE_NEGATIVE("INVALID VALUE {} : must be greater than or equal to 0"),
    CANNOT_SCALE_DOWN_ZERO("INVALID VALUE {} : scale down by value must be greater than 0"),
    TIMEOUT_WHILE_WAITING_FOR_DEPLOYMENT("Timedout while waiting for deployment",HyscaleErrorGroup.WAIT_FOR_DEPLOYMENT),
    CANNOT_SCALE_OUT_RANGE_HPA("Operation involves scaling out of the autoscaling range [{}-{}] configured");

    private String message;
    private int code;

    DeployerErrorCodes(String message) {
        this.message = message;
    }

    DeployerErrorCodes(String message, HyscaleErrorGroup errorGroup){
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
