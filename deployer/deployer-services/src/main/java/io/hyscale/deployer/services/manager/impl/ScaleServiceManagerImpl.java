/**
 * Copyright 2019 Pramati Prism, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.impl.V1PodHandler;
import io.hyscale.deployer.services.manager.ScaleServiceManager;
import io.hyscale.deployer.services.model.PodParent;
import io.hyscale.deployer.services.model.ScaleSpec;
import io.hyscale.deployer.services.model.ScaleStatus;
import io.kubernetes.client.openapi.ApiClient;
import org.springframework.stereotype.Component;

@Component
public class ScaleServiceManagerImpl implements ScaleServiceManager {

    @Override
    public ScaleStatus scale(ApiClient apiClient, String appName, String service, String namespace, ScaleSpec scaleSpec) throws HyscaleException {
        V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        String selector = ResourceSelectorUtil.getServiceSelector(appName, service);
        PodParent podParent = podHandler.getPodParent(apiClient, selector, namespace);
        return null;
    }
}
