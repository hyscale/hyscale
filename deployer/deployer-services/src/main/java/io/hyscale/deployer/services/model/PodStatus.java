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
package io.hyscale.deployer.services.model;

import io.hyscale.deployer.core.model.DeploymentStatus;

import java.util.HashMap;
import java.util.Map;

public enum PodStatus {

    IMAGEPULL_BACKOFF("ImagePullBackOff", true, DeploymentStatus.ServiceStatus.FAILED),
    CRASHLOOP_BACKOFF("CrashLoopBackOff", true, DeploymentStatus.ServiceStatus.FAILED),
    PENDING("Pending", true, DeploymentStatus.ServiceStatus.FAILED),
    ERR_IMAGE_PULL("ErrImagePull", true, DeploymentStatus.ServiceStatus.FAILED),
    OOMKILLED("OOMKilled", true, DeploymentStatus.ServiceStatus.FAILED),
    RUN_CONTAINER_ERROR("RunContainerErr", true, DeploymentStatus.ServiceStatus.FAILED),
    ERROR("Error", true, DeploymentStatus.ServiceStatus.FAILED),
    COMPLETED("Completed", true, DeploymentStatus.ServiceStatus.FAILED),
    RUNNING("Running", false, DeploymentStatus.ServiceStatus.RUNNING),
    DEFAULT("default", true, DeploymentStatus.ServiceStatus.FAILED),
    TERMINATING("Terminating", false, DeploymentStatus.ServiceStatus.NOT_RUNNING);

    private String status;
    private boolean failed;
    private DeploymentStatus.ServiceStatus serviceStatus;
    private static Map<String, PodStatus> podStatusMap = new HashMap<>();

    PodStatus(String status, boolean failed, DeploymentStatus.ServiceStatus serviceStatus) {
        this.status = status;
        this.failed = failed;
        this.serviceStatus = serviceStatus;
    }

    static {
        for (PodStatus each : PodStatus.values()) {
            podStatusMap.put(each.getStatus(), each);
        }
    }

    public String getStatus() {
        return status;
    }

    public boolean isFailed() {
        return failed;
    }

    public static PodStatus get(String status) {
        return podStatusMap.getOrDefault(status, PodStatus.DEFAULT);
    }

    public DeploymentStatus.ServiceStatus getServiceStatus() {
        return serviceStatus;
    }
}
