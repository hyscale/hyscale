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
    SCHEMA_VALIDATION_ERROR("Invalid input spec {}\n {}"),
    FAILED_TO_DESERIALIZE_REPLICAS("Cannot process the field 'replicas' with error {} , deployment continued with min 1 replica"),
    SERVICE_NAME_MISMATCH("Service name mismatch with hspec file name convention as <service-name>.hspec"),
    PROFILE_NAME_MISMATCH("profile name mismatch with hprof file name convention as <environment-name>-<service-name>.hprof"),
    IMPROPER_SERVICE_FILE_NAME("Recommended file name pattern for service spec is <service-name>.hspec"),
    IMPROPER_PROFILE_FILE_NAME("Recommended file name pattern for profile is <environment>-<service-name>.hprof"),
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
