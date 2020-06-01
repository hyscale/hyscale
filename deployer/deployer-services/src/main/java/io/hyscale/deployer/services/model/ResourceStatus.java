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

/**
 * Status of resource on cluster
 *
 */
public enum ResourceStatus {
    PENDING(DeploymentStatus.ServiceStatus.NOT_RUNNING),    // Resource not yet deployed
    STABLE(DeploymentStatus.ServiceStatus.RUNNING),         // Resource deployed successfully
    PAUSED(DeploymentStatus.ServiceStatus.NOT_RUNNING),     // Resource not yet deployed
    FAILED(DeploymentStatus.ServiceStatus.FAILED);          // Resource failed

    private DeploymentStatus.ServiceStatus serviceStatus;

    private ResourceStatus(DeploymentStatus.ServiceStatus serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public DeploymentStatus.ServiceStatus getServiceStatus() {
        return serviceStatus;
    }

    public static DeploymentStatus.ServiceStatus getServiceStatus(ResourceStatus resourceStatus) {
        return resourceStatus == null ? DeploymentStatus.ServiceStatus.NOT_DEPLOYED : resourceStatus.getServiceStatus();
    }
}