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

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ResourceLabelUtil;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.model.ScaleOperation;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class PodParentHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(PodParentHandler.class);

    public abstract List<DeploymentStatus> getStatus(ApiClient apiClient, String selector, boolean label, String namespace);

    public abstract DeploymentStatus buildStatus(T t);

    public abstract List<DeploymentStatus> buildStatus(List<T> t);
    
    public abstract List<T> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace)
            throws HyscaleException;

    /**
     * Get all resources irrespective of namespace
     * @param apiClient
     * @param selector
     * @param label
     * @return List of resource
     * @throws HyscaleException
     */
    public abstract List<T> listForAllNamespaces(ApiClient apiClient, String selector, boolean label)
            throws HyscaleException;

    protected abstract String getPodRevision(ApiClient apiClient, T t);

    public abstract Integer getReplicas(T t);

    protected abstract String getPodRevision(ApiClient apiClient, String selector, boolean label, String namespace);

    public abstract boolean scale(ApiClient apiClient, T t, String namespace,  int value) throws HyscaleException;

    public DeploymentStatus buildStatusFromMetadata(V1ObjectMeta metadata, DeploymentStatus.ServiceStatus serviceStatus) {
        if (metadata == null) {
            return null;
        }
        DeploymentStatus deploymentStatus = new DeploymentStatus();
        String serviceName = ResourceLabelUtil.getServiceName(metadata.getLabels());
        deploymentStatus.setServiceName(serviceName);
        deploymentStatus.setServiceStatus(serviceStatus);
        deploymentStatus.setAge(metadata.getCreationTimestamp());
        return deploymentStatus;
    }

    public abstract String getKind();

    public String getPodSelector(ApiClient apiClient, String selector, boolean label, String namespace) {
        if (selector == null) {
            return null;
        }
        String revision = getPodRevision(apiClient, selector, label, namespace);
        return revision != null ? selector.concat("," + revision) : selector;
    }

    public String getPodSelector(ApiClient apiClient, T t, String selector) {
        if (t == null) {
            return selector;
        }
        String revision = getPodRevision(apiClient, t);
        if (selector == null && revision != null) {
            return revision;
        }
        return revision != null ? selector.concat("," + revision) : selector;
    }

    public int getDesiredReplicas(ScaleOperation scaleOp, int scaleValue, int currentReplicas) throws HyscaleException {
        V1Scale exisiting = new V1ScaleBuilder()
                .withSpec(new V1ScaleSpec().replicas(currentReplicas))
                .build();
        int desiredReplicas = currentReplicas;
        switch (scaleOp) {
            case SCALE_TO:
                desiredReplicas = scaleValue;
                break;
            case SCALE_UP:
                desiredReplicas = currentReplicas + scaleValue;
                break;
            case SCALE_DOWN:
                if (scaleValue == 0) {
                    throw new HyscaleException(DeployerErrorCodes.CANNOT_SCALE_DOWN_ZERO, String.valueOf(scaleValue));
                }
                if (currentReplicas > 0) {
                    desiredReplicas = currentReplicas - scaleValue;
                }
                break;
        }
        desiredReplicas = desiredReplicas < 0 ? 0 : desiredReplicas;
        logger.debug("Preparing the scale patch , desired replicas {} ", desiredReplicas);

        return desiredReplicas;
    }
    
}

