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
import io.hyscale.deployer.services.factory.PodParentFactory;
import io.hyscale.deployer.services.handler.PodParentHandler;
import io.hyscale.deployer.services.model.PodParent;
import io.kubernetes.client.openapi.ApiClient;

@Component
public class PodParentProvider {

    /**
     * For all available {@link PodParentHandler} 
     * Returns the first available pod parent
     * 
     * @param apiClient
     * @param selector
     * @param label
     * @param namespace
     * @return {@link PodParent}
     * @throws HyscaleException
     */
    public PodParent getPodParent(ApiClient apiClient, String selector, boolean label, String namespace)
            throws HyscaleException {
        List<PodParentHandler> podParentHandlerList = PodParentFactory.getAllHandlers();

        for (PodParentHandler podParentHandler : podParentHandlerList) {
            PodParent podParent = podParentHandler.getPodParent(apiClient, selector, label, namespace);
            if (podParent != null) {
                return podParent;
            }
        }
        return null;
    }

    /**
     * For all available {@link PodParentHandler} 
     * Combine the available {@link PodParent} in the given namespace
     * 
     * @param apiClient
     * @param selector
     * @param label
     * @param namespace
     * @return list of {@link PodParent}
     * @throws HyscaleException
     */
    public List<PodParent> getPodParentsList(ApiClient apiClient, String selector, boolean label,
            String namespace) throws HyscaleException {
        List<PodParentHandler> podParentHandlerList = PodParentFactory.getAllHandlers();
        List<PodParent> podParentList = new ArrayList<PodParent>();
        for (PodParentHandler podParentHandler : podParentHandlerList) {
            List<PodParent> podParents = podParentHandler.getPodParentsList(apiClient, selector, label, namespace);
            if (podParents != null && !podParents.isEmpty()) {
                podParentList.addAll(podParents);
            }
        }
        return podParentList;
    }

    /**
     * 
     * @param apiClient
     * @param selector
     * @param label
     * @param namespace
     * @return true if any pod parent resource exists
     * @throws HyscaleException
     */
    public boolean podParentExists(ApiClient apiClient, String selector, boolean label, String namespace)
            throws HyscaleException {
        List<PodParentHandler> podParentHandlerList = PodParentFactory.getAllHandlers();
        for (PodParentHandler podParentHandler : podParentHandlerList) {
            PodParent podParent = podParentHandler.getPodParent(apiClient, selector, label, namespace);
            if (podParent != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * For all available {@link PodParentHandler} 
     * Combine the available {@link PodParent} across namespaces
     * 
     * @param apiClient
     * @param selector
     * @param label
     * @return list of {@link PodParent}
     * @throws HyscaleException
     */
    public List<PodParent> getParentsForAllNamespaces(ApiClient apiClient, String selector, boolean label)
            throws HyscaleException {
        List<PodParentHandler> podParentHandlerList = PodParentFactory.getAllHandlers();
        List<PodParent> podParentList = new ArrayList<PodParent>();
        for (PodParentHandler podParentHandler : podParentHandlerList) {
            List<PodParent> podParents = podParentHandler.getParentsForAllNamespaces(apiClient, selector, label);
            if (podParents != null && !podParents.isEmpty()) {
                podParentList.addAll(podParents);
            }
        }
        return podParentList;
    }
}
