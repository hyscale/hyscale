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

public enum AbstractedErrorMessage implements IMessage, IReason {

    CONTACT_CLUSTER_ADMINISTRATOR("Please contact your cluster administrator.", "{}"),
    CANNOT_INFER_ERROR("Cannot determine cause of failure from existing state of deployment.", ""),
    IMAGE_NOT_FOUND("Cannot find image {} ", "Please fix the image name"),
    IMAGEPULL_BACKOFF_ACTION("Issue can be due to invalid credentials of image registry in your hspec or it is not reachable to the cluster {}",
            "Please check them at {}"),
    //TODO specify one of these
    INVALID_STORAGE_CLASS("Invalid storage class defined for volumes for service {} in hspec.", "Please provide in one of these {} in your cluster"),
    NO_STORAGE_CLASS_FOUND("No storage class have been defined in your cluster ", "Please ask your cluster administrator to define storage class in your cluster"),
    NOT_ENOUGH_MEMORY_FOUND("Not enough memory defined for {} to run", "Increase the memory limits of {} Eg: memory: 512Mi (equivalent to 512MB )"),
    INVALID_STARTCOMMANDS_FOUND("{} should provide long running task for the container to run either in Dockerfile or startCommand of {} hspec","Provide a long running task for the container to run"),
    APPLICATION_CRASH("Your service might be crashing due to invalid startup commands in hspec / invalid run commands in buildSpec of hspec / invalid CMD in Dockerfile", "{}"),
    LIVENESS_PROBE_FAILURE("Healthcheck defined for your service {} hspec might be invalid. {} ", "Possible fixes can be 1. App should listen on 0.0.0.0. 2. Fix healthcheck in hspec"),
    CLUSTER_FULL("Cannot accomodate new services as cluster is full.", " Please contact your cluster administrator to provision a bigger cluster"),
    FIX_IMAGE_NAME("Image name /tag in {} hspec found to be invalid ", "Please fix the image name/tag in {} hspec"),
    INVALID_PULL_REGISTRY_CREDENTIALS("Image registry credentials found invalid at {} ", "Please verify them and update with the correct credentials of the registry"),
    DOCKERFILE_CMD_UNCERTAINITY("Cannot determine whether CMD is present in Dockerfile or not of service {}. With this uncertainty, the troubleshooted results are invalid if you find a missing CMD in Dockerfile.", "{}"),
    OOMKILLED("Service failed due to insufficient memory .Please increase memory of your service {} in hspec with memory directive [Eg: memory:512Mi]", "{}"),
    SERVICE_NOT_DEPLOYED("Service {} is deployed in this cluster", "{}"),
    CANNOT_FIND_EVENTS("Cannot find kubernetes events in the cluster. The results might be inappropriate", "Please try to redeploy and troubleshoot the actual error");

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
        if (s == null || s == null) {
            return "";
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
