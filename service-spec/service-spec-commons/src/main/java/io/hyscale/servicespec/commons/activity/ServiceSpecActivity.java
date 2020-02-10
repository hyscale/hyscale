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
package io.hyscale.servicespec.commons.activity;

import io.hyscale.commons.models.Activity;

public enum ServiceSpecActivity implements Activity {
    FAILED_TO_DESERIALIZE_REPLICAS("Cannot process the field 'replicas' with error {} , deployment continued with min 1 replica"),
    SERVICE_NAME_MISMATCH("Service spec file convention to be <service-name>.hspec.yaml for better readability,But {} did not match with service name given in the service spec."),
    PROFILE_NAME_MISMATCH("Expecting profile file name convention to be <env-name>-<service-name>.hprof.yaml for better readability,But {} does not match with env or service names specified in the profile file"),
    INVALID_SERVICE_SPEC_NAME_MSG ("Recommended file name pattern for service spec is <service-name>.hspec.yaml."),
    INVALID_PROFILE_FILE_NAME_MSG ("Recommended file name pattern for profile is <profile-name>-<service-name>.hprof.yaml."),
    ERROR("{}");


    String message;

    ServiceSpecActivity(String message) {
        this.message = message;
    }

    @Override
    public String getActivityMessage() {
        return this.message;
    }
}
