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
package io.hyscale.troubleshooting.integration.models;

import org.apache.commons.lang3.StringUtils;

public enum AbstractedErrorMessage implements IMessage, IReason {

    CONTACT_CLUSTER_ADMINISTRATOR("Please contact your cluster administrator", ""),
    CANNOT_INFER_ERROR("Cannot determine cause of failure from existing state of deployment", "Try redeploying or contact your cluster administrator. Kubernetes error message follows: \n {} "),
    IMAGEPULL_BACKOFF_ACTION("Incorrect registry credentials", "Check them at {}"),
    INVALID_STORAGE_CLASS("Incorrect storage class for volumes in \"{}\" service spec", "Provide any one of these \"{}\""),
    NO_STORAGE_CLASS_FOUND("Cannot provision new volumes, no storage class configured in your cluster", "Please contact your cluster administrator"),
    NOT_ENOUGH_MEMORY_FOUND("Out of memory errors. Not enough memory to run \"{}\"", "Increase the memory limits in service spec and try redeploying"),
    INVALID_STARTCOMMANDS_FOUND("Service container exited abruptly", "Possible incorrect startCommands in service spec or CMD in Dockerfile"),
    APPLICATION_CRASH("Service observed to be crashing", "Please verify the startCommands in service spec or CMD in Dockerfile"),
    LIVENESS_PROBE_FAILURE("Health check specified for service failed 3 times in succession", ""),
    CLUSTER_FULL("Cannot accommodate new services as the cluster is full", "Please contact your cluster administrator to add cluster capacity or deploy to a different cluster"),
    FIX_IMAGE_NAME("Invalid Image name or tag provided", "Recheck the image name or tag in {} service spec"),
    INVALID_PULL_REGISTRY_CREDENTIALS("Invalid target registry credentials for \"{}\"", "Check them at {}"),
    DOCKERFILE_CMD_UNCERTAINITY("Service observed to be crashing. Possible errors in ENTRYPOINT/ CMD in Dockerfile or missing ENTRYPOINT", ""),
    SERVICE_NOT_DEPLOYED("No such service found in this cluster", "Ensure you are querying for the correct service name in the correct namespace and cluster"),
    SERVICE_WITH_ZERO_REPLICAS("Service deployed in cluster with 0 replicas",""),
    CANNOT_FIND_EVENTS("Cannot determine cause of failure since this service deployment is older than 60 minutes", "Try redeploying to troubleshoot"),
    INVALID_VOLUME_NAME("Volume name provided in service spec is invalid", "Volume name must be less than 63 characters and must follow the regex [a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*"),
    MULTIPLE_DEFAULT_STORAGE_CLASS("Volume creation failed. More than 1 default storage class configured on the cluster","Provide a storage class name in the service spec or contact your cluster administrator to configure a single default storage class"),
    INVALID_RESOURCE_NAME("{} available in cluster is invalid", "Fix the resource name and redeploy"),
    TRY_AFTER_SOMETIME("Deployment is still in progress, service is not yet ready", "Try querying after sometime"),
    SERVICE_COMMANDS_FAILURE("Service startup commands failed with {}" , "Possible errors can be in startCommands of hspec or ENTRYPOINT/CMD in Dockerfile");

    private String message;
    private String reason;

    AbstractedErrorMessage(String reason, String message) {
        this.reason = reason;
        this.message = message;
    }

    public String formatMessage(String... args) {
        return format(message, args);
    }

    public String formatReason(String... args) {
        return format(reason, args);
    }

    private String format(String s, String... args) {
        if (s == null) {
            return StringUtils.EMPTY;
        }
        return args != null && args.length != 0 ? String.format(s.replaceAll("\\{\\}", "%s"), args)
                : s;
    }

    @Override
    public String getReason() {
        return reason;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
