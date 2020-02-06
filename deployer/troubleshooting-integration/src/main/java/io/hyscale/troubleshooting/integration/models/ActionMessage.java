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

import io.hyscale.commons.models.Activity;

public enum ActionMessage implements Activity {
    CONTACT_CLUSTER_ADMINISTRATOR("Please contact your cluster administrator {}"),
    CANNOT_INFER_ERROR("Cannot infer error from existing state of deployment . Please contact your cluster administrator"),
    IMAGEPULL_BACKOFF_ACTION("Issue can be due to invalid credentials of image registry in your hspec." +
            " If you are hspec is without any image building, please check if your image name & tag exists . Please check them at {}"),
    //TODO specify one of these
    INVALID_STORAGE_CLASS("Invalid storage class defined for volume {} for service {} in hspec."),
    APPLICATION_CRASH("Your service might be crashing due to invalid startup commands in hspec / invalid run commands in buildSpec of hspec / invalid CMD in Dockerfile"),
    LIVENESS_PROBE_FAILURE("Healthcheck defined for your service {} hspec might be invalid. App should listen on 0.0.0.0 , healthcheck is made on pod ip and configured port"),
    CLUSTER_FULL("Cannot accomodate new services as cluster is full . Please contact your cluster administrator to provision a bigger cluster"),
    FIX_IMAGE_NAME("Issue is in your image name, please fix the image"),
    DOCKERFILE_CMD_UNCERTAINITY("Cannot determine whether CMD is present in Dockerfile or not of service {}. With this uncertainty, the troubleshooted results are invalid if you find a missing CMD in Dockerfile."),
    OOMKILLED("Service failed due to insufficient memory .Please increase memory of your service {} in hspec with memory directive [Eg: memory:512Mi] ");


    private String message;

    ActionMessage(String message) {
        this.message = message;
    }

    @Override
    public String getActivityMessage() {
        return null;
    }

    public String getMessage() {
        return message;
    }
}
