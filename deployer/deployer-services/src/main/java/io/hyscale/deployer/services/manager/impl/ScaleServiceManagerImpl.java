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
package io.hyscale.deployer.services.manager.impl;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.factory.PodParentFactory;
import io.hyscale.deployer.services.handler.PodParentHandler;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.impl.V1HorizontalPodAutoScalerHandler;
import io.hyscale.deployer.services.manager.ScaleServiceManager;
import io.hyscale.deployer.services.model.*;
import io.hyscale.deployer.services.processor.PodParentProvider;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1HorizontalPodAutoscaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScaleServiceManagerImpl implements ScaleServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(ScaleServiceManagerImpl.class);

    @Autowired
    private PodParentProvider podParentProvider;

    @Override
    public ScaleStatus scale(ApiClient apiClient, String appName, String service, String namespace, ScaleSpec scaleSpec) throws HyscaleException {
        PodParent podParent = podParentProvider.getPodParent(apiClient, appName, service, namespace);
        if (podParent == null) {
            logger.error("Error while fetching pod parent of service {} in namespace {} ", service, namespace);
            throw new HyscaleException(DeployerErrorCodes.SERVICE_NOT_DEPLOYED, service, namespace, appName);
        }

        PodParentHandler podParentHandler = PodParentFactory.getHandler(podParent.getKind());
        boolean status = false;
        ScaleStatus scaleStatus = new ScaleStatus();

        int replicas = podParentHandler.getReplicas(podParent.getParent());
        int desiredReplicas = podParentHandler.getDesiredReplicas(scaleSpec.getScaleOp(), scaleSpec.getValue(), replicas);
        logger.debug("Scaling service {} from replicas to desired state {}",service,replicas,desiredReplicas);
        V1HorizontalPodAutoScalerHandler autoScalerHandler = (V1HorizontalPodAutoScalerHandler) ResourceHandlers.getHandlerOf(ResourceKind.HORIZONTAL_POD_AUTOSCALER.getKind());
        List<V1HorizontalPodAutoscaler> podAutoscalers = autoScalerHandler.getBySelector(apiClient, ResourceSelectorUtil.getServiceSelector(appName, service), true, namespace);
        if (podAutoscalers != null && !podAutoscalers.isEmpty()) {
            // When you are scaling up from zero replicas to a non-zero replicas when HPA is enabled. Because here HPA maintains min replica policy
            if(replicas==0){
                WorkflowLogger.persist(DeployerActivity.DESIRED_STATE_ON_HPA_ENABLED);
            }else {
                validate(podAutoscalers.get(0), desiredReplicas);
            }
        }

        status = podParentHandler.scale(apiClient, podParent.getParent(), namespace, desiredReplicas);
        if (!status) {
            throw new HyscaleException(DeployerErrorCodes.TIMEDOUT_WHILE_WAITING_FOR_SCALING, service);
        }
        scaleStatus.setSuccess(status);
        return scaleStatus;
    }

    private void validate(V1HorizontalPodAutoscaler v1HorizontalPodAutoscaler, int desiredReplicas) throws HyscaleException {
        if (v1HorizontalPodAutoscaler == null) {
            return;
        }
        if (desiredReplicas == 0) {
           return;
        }

        if (desiredReplicas < v1HorizontalPodAutoscaler.getSpec().getMinReplicas() || desiredReplicas > v1HorizontalPodAutoscaler.getSpec().getMaxReplicas()) {
            throw new HyscaleException(DeployerErrorCodes.CANNOT_SCALE_OUT_RANGE_HPA, String.valueOf(v1HorizontalPodAutoscaler.getSpec().getMinReplicas()),
                    String.valueOf(v1HorizontalPodAutoscaler.getSpec().getMaxReplicas()));
        }
    }
}
