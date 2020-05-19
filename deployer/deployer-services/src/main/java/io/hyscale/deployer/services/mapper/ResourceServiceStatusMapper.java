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
package io.hyscale.deployer.services.mapper;

import java.util.HashMap;
import java.util.Map;

import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.services.model.ResourceStatus;

public class ResourceServiceStatusMapper {

    private static Map<ResourceStatus, DeploymentStatus.ServiceStatus> resourceVsServiceStatus = new HashMap<ResourceStatus, DeploymentStatus.ServiceStatus>();

    static {
        resourceVsServiceStatus.put(ResourceStatus.FAILED, DeploymentStatus.ServiceStatus.FAILED);
        resourceVsServiceStatus.put(ResourceStatus.PAUSED, DeploymentStatus.ServiceStatus.NOT_RUNNING);
        resourceVsServiceStatus.put(ResourceStatus.PENDING, DeploymentStatus.ServiceStatus.NOT_RUNNING);
        resourceVsServiceStatus.put(ResourceStatus.STABLE, DeploymentStatus.ServiceStatus.RUNNING);
        resourceVsServiceStatus.put(null, DeploymentStatus.ServiceStatus.NOT_DEPLOYED);
    }

    public static DeploymentStatus.ServiceStatus getServiceStatus(ResourceStatus resourceStatus) {
        return resourceVsServiceStatus.get(resourceStatus);
    }
}
