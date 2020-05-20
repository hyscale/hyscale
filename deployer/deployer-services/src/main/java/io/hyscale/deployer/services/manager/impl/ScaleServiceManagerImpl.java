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
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.deployer.Deployer;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.factory.PodParentFactory;
import io.hyscale.deployer.services.handler.PodParentHandler;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.impl.V1PodHandler;
import io.hyscale.deployer.services.manager.ScaleServiceManager;
import io.hyscale.deployer.services.model.PodParent;
import io.hyscale.deployer.services.model.ScaleSpec;
import io.hyscale.deployer.services.model.ScaleStatus;
import io.kubernetes.client.openapi.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScaleServiceManagerImpl implements ScaleServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(ScaleServiceManagerImpl.class);

    @Override
    public ScaleStatus scale(ApiClient apiClient, String appName, String service, String namespace, ScaleSpec scaleSpec) throws HyscaleException {
        V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        String selector = ResourceSelectorUtil.getServiceSelector(appName, service);
        PodParent podParent = podHandler.getPodParent(apiClient, selector, namespace);
        if (podParent == null) {
            logger.error("Error while fetching pod parent of service {} in namespace {} ", service, namespace);
            throw new HyscaleException(DeployerErrorCodes.SERVICE_NOT_DEPLOYED, service, namespace, appName);
        }

        PodParentHandler podParentHandler = PodParentFactory.getHandler(podParent.getKind());
        boolean status = false;
        ScaleStatus scaleStatus = new ScaleStatus();
        status = podParentHandler.scale(apiClient, podParent.getParent(), namespace, scaleSpec.getScaleOp(), scaleSpec.getValue());
        if (!status) {
            throw new HyscaleException(DeployerErrorCodes.TIMEDOUT_WHILE_WAITING_FOR_SCALING, service);
        }
        scaleStatus.setSuccess(status);
        return scaleStatus;
    }
}
