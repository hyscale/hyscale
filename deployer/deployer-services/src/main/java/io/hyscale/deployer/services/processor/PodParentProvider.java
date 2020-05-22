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
package io.hyscale.deployer.services.processor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.services.factory.PodParentFactory;
import io.hyscale.deployer.services.handler.PodParentHandler;
import io.hyscale.deployer.services.model.PodParent;
import io.kubernetes.client.openapi.ApiClient;

@Component
public class PodParentProvider {

    /**
     * 
     * @param apiClient
     * @param appName
     * @param serviceName
     * @param namespace
     * @return {@link PodParent} for the app and service in the given namespace
     * @throws HyscaleException
     */
    public PodParent getPodParent(ApiClient apiClient, String appName, String serviceName, String namespace)
            throws HyscaleException {
        List<PodParentHandler> podParentHandlerList = PodParentFactory.getAllHandlers();
        String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
        for (PodParentHandler podParentHandler : podParentHandlerList) {
            List podParentResource = podParentHandler.getBySelector(apiClient, selector, true, namespace);
            if (podParentResource != null && !podParentResource.isEmpty()) {
                return new PodParent(podParentHandler.getKind(), podParentResource.get(0));
            }
        }
        return null;
    }

    /**
     * Provides a list of {@link PodParent} for the app in given namespace
     * 
     * @param apiClient
     * @param appName
     * @param namespace
     * @return list of {@link PodParent}
     * @throws HyscaleException
     */
    public List<PodParent> getPodParents(ApiClient apiClient, String appName, String namespace)
            throws HyscaleException {
        List<PodParent> podParentList = new ArrayList<PodParent>();
        List<PodParentHandler> podParentHandlerList = PodParentFactory.getAllHandlers();
        String selector = ResourceSelectorUtil.getSelector(appName);
        for (PodParentHandler podParentHandler : podParentHandlerList) {
            List podParentResource = podParentHandler.getBySelector(apiClient, selector, true, namespace);
            if (podParentResource != null) {
                podParentResource.stream().forEach(each -> {
                    PodParent podParent = new PodParent(podParentHandler.getKind(), each);
                    podParentList.add(podParent);
                });
            }
        }
        return podParentList;
    }

    /**
     * 
     * @param apiClient
     * @param appName
     * @param serviceName
     * @param namespace
     * @return true if {@link PodParent} exists for the app and service
     * @throws HyscaleException
     */
    public boolean hasPodParent(ApiClient apiClient, String appName, String serviceName, String namespace)
            throws HyscaleException {
        return getPodParent(apiClient, appName, serviceName, namespace) != null ? true : false;
    }

    /**
     * Provides list of all {@link PodParent} available in the cluster
     * 
     * @param apiClient
     * @return list of {@link PodParent}
     * @throws HyscaleException
     */
    public List<PodParent> getAllPodParents(ApiClient apiClient) throws HyscaleException {
        List<PodParent> podParentList = new ArrayList<PodParent>();
        List<PodParentHandler> podParentHandlerList = PodParentFactory.getAllHandlers();
        for (PodParentHandler podParentHandler : podParentHandlerList) {
            List podParentResource = podParentHandler.listForAllNamespaces(apiClient, null, true);
            if (podParentResource != null) {
                podParentResource.stream().forEach(each -> {
                    PodParent podParent = new PodParent(podParentHandler.getKind(), each);
                    podParentList.add(podParent);
                });
            }
        }
        return podParentList;
    }
}
