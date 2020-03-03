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
package io.hyscale.deployer.services.handler;

import java.util.List;

import io.hyscale.commons.utils.ResourceLabelUtil;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

public abstract class PodParentHandler<T> {

    public abstract List<DeploymentStatus> getStatus(ApiClient apiClient, String selector, boolean label,
            String namespace);

    public abstract DeploymentStatus buildStatus(T t);

    public abstract List<DeploymentStatus> buildStatus(List<T> t);

    public DeploymentStatus buildStatusFromMetadata(V1ObjectMeta metadata, DeploymentStatus.Status status) {
        if (metadata == null) {
            return null;
        }
        DeploymentStatus deploymentStatus = new DeploymentStatus();
        String serviceName = ResourceLabelUtil.getServiceName(metadata.getLabels());
        deploymentStatus.setServiceName(serviceName);
        deploymentStatus.setStatus(status);
        deploymentStatus.setAge(metadata.getCreationTimestamp());
        return deploymentStatus;
    }
}
