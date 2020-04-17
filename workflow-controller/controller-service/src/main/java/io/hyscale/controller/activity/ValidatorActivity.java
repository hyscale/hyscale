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

public enum ValidatorActivity implements Activity {
    VALIDATING_SERVICE_SPEC("Validating service spec(s) "),
    VALIDATING_PROFILE("Validating profile(s) "),
    VALIDATING_DOCKER("Validating docker "),
    VALIDATING_CLUSTER("Validating cluster "),
    VALIDATING_MANIFEST("Validating manifest for service {} "),
    VALIDATING_REGISTRY("Validating registry {} "),
    VALIDATING_VOLUME("Validating volumes for service {} "),
    VOLUME_VALIDATION_FAILED("Volume validation failed. Error {}"),
    MANIFEST_VALIDATION_FAILED("Manifest validation failed. Error {}"),
    DOCKER_NOT_INSTALLED("Docker not installed. Install docker to continue "),
    DOCKER_DAEMON_NOT_RUNNONG("Docker daemon not running. Start Docker to continue "),
    MISSING_DOCKER_REGISTRY_CREDENTIALS("Cannot find {} credentials. Do 'docker login {}' to continue with the deployment"),
    SERVICE_SPEC_VALIDATION_FAILED("Service spec validation failed. {} "),
    PROFILE_VALIDATION_FAILED("Profile validation failed. {} ");

    private String message;

    ValidatorActivity(String message) {
        this.message = message;
    }

    @Override
    public String getActivityMessage() {
        return message;
    }

}
