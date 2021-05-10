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

/**
 * Activities list for validation
 *
 */
public enum ValidatorActivity implements Activity {
    VALIDATING_SERVICE_SPEC("Validating service spec(s) "),
    VALIDATING_PROFILE("Validating profile(s) "),
    VALIDATING_MAPPING("Validating Service and Profile mapping"),
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
    SERVICE_SPEC_VALIDATION_FAILED("Service spec validation failed for {}. {} "),
    PROFILE_VALIDATION_FAILED("Profile validation failed for {}. {} "),
    CLUSTER_AUTHENTICATION_FAILED("Cluster authentication failed. Verify if cluster config is valid"),
    MISSING_PORTS("Invalid Service Spec. Missing required field 'ports' in allowTraffic."),
    INVALID_EXTERNAL_VALUE("Invalid Service Spec. Traffic rules cannot be applied to a service exposed externally."),
    DUPLICATE_PORTS("Invalid Service Spec. Duplicate ports found : {}"),
    PORT_NOT_EXPOSED("Invalid Service Spec. Traffic rules cannot be applied to a port that is not exposed on the Service"),
    //Load balancer validation
    PORTS_MISMATCH("Ports declared in the load balancer should match with the ports mentioned in the hspec. Mismatched port : {}"),
    EXTERNAL_CONFIGURED("LoadBalancer configuration requires external field to be set as true in hspec."),
    LB_GATEWAY_LABEL_REQUIRED("LoadBalancer gateway label is required for istio."),
    LB_PROVIDER_REQUIRED("LoadBalancer provider name is required."),
    LB_HOST_REQUIRED("LoadBalancer host name is required."),
    LB_MAPPING_REQUIRED("Path mapping for loadBalancer is required."),
    LB_PORT_REQUIRED("Invalid loadbalancer configuration. Port is required {}."),
    LB_CONTEXT_PATH_REQUIRED("Invalid loadbalancer configuration. Atleast one contextPath is required {}.");
    private String message;

    ValidatorActivity(String message) {
        this.message = message;
    }

    @Override
    public String getActivityMessage() {
        return message;
    }

}
