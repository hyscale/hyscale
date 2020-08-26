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
package io.hyscale.generator.services.model;

import io.hyscale.commons.models.Activity;

public enum ManifestGeneratorActivity implements Activity {
    MISSING_FIELD("Missing field {}"),
    STATEFUL_SET("Generating StatefulSet manifest "),
    DEPLOYMENT("Generating Deployment manifest "),
    ERROR_WHILE_PROCESSING_MANIFEST_PLUGINS("Error while generating manifest plugins "),
    GENERATING_MANIFEST("Generating manifest for {} "),
    INVALID_SIZE_FORMAT("Invalid size format {} "),
    INSUFFICIENT_MEMORY("Ignoring memory limits as 4Mi is minimum memory but declared is {}"),
    INSUFFICIENT_CPU("Ignoring CPU limits as 1m is minimum memory but declared is {}"),
    INVALID_RANGE("Invalid range {}"),
    FAILED_TO_CREATE_IMAGE_PULL_SECRET("Missing credentials for the registry {}. Please perform 'docker login {} ' to continue with the deployment"),
    FAILED_TO_PROCESS_REPLICAS("Unexpected error while processing replicas"),
    IGNORING_REPLICAS("{} , so replicas has been ignored for this deployment"),
    FAILED_TO_READ_CUSTOM_SNIPPETS("Failed to read K8s Snippets from file {}"),
    INVALID_CUSTOM_SNIPPET("Provide a valid YAML snippet for K8s Snippets in {}"),
    IGNORING_CUSTOM_SNIPPET("K8s Snippets not supported for kinds {}"),
    APPLYING_CUSTOM_SNIPPET("Applying K8s Snippets for {}");
    private String message;

    private ManifestGeneratorActivity(String message) {
        this.message = message;
    }

    @Override
    public String getActivityMessage() {
        return message;
    }

}
