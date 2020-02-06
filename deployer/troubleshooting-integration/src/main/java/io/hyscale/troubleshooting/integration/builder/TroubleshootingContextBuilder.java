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
package io.hyscale.troubleshooting.integration.builder;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.handler.impl.V1EventHandler;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.troubleshooting.integration.errors.TroubleshootErrorCodes;
import io.hyscale.troubleshooting.integration.models.ServiceInfo;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TroubleshootingContextBuilder {

    private static final Logger logger = LoggerFactory.getLogger(TroubleshootingContextBuilder.class);

    @Autowired
    private K8sClientProvider k8sClientProvider;

    public TroubleshootingContext build(@NonNull ServiceInfo serviceInfo, @NonNull K8sAuthorisation k8sAuthorisation, @NonNull String namespace) throws HyscaleException {
        TroubleshootingContext context = new TroubleshootingContext();
        try {
            ApiClient apiClient = k8sClientProvider.get(k8sAuthorisation);

            context.setServiceInfo(serviceInfo);
            // context.setResourceEvents(getResourceEvents(apiClient, namespace));
            //context.setResources(getResources(serviceInfo, apiClient, namespace));
            context.setResourceData(getResourceData(getResourceEvents(apiClient, namespace), getResources(serviceInfo, apiClient, namespace)));

        } catch (HyscaleException e) {
            logger.error("Error while preparing context to troubleshoot the service {}", serviceInfo.getServiceName());
            throw e;
        }
        return context;
    }

    private Map<String, TroubleshootingContext.ResourceData> getResourceData(Map<String, List<V1Event>> resourceEvents, Map<String, List<Object>> resources) {
        Map<String, TroubleshootingContext.ResourceData> resourceDataMap = new HashMap();
        if (resourceEvents != null) {
            resourceEvents.entrySet().stream().forEach(each -> {

                TroubleshootingContext.ResourceData resourceData = new TroubleshootingContext.ResourceData();
                resourceData.setEvents(each.getValue());
                resourceDataMap.put(each.getKey(), resourceData);

            });
        }
        if (resources != null) {
            resources.entrySet().stream().forEach(each -> {
                TroubleshootingContext.ResourceData resourceData = null;
                if (!resourceDataMap.containsKey(each.getKey())) {
                    resourceData = new TroubleshootingContext.ResourceData();
                    resourceDataMap.put(each.getKey(), resourceData);
                }
                resourceData = resourceDataMap.get(each.getKey());
                resourceData.setResource(each.getValue());
            });
        }
        return resourceDataMap;
    }

    private Map<String, List<Object>> getResources(@NonNull ServiceInfo serviceInfo, @NonNull ApiClient apiClient, @NonNull String namespace) throws HyscaleException {

        String selector = ResourceSelectorUtil.getSelector(serviceInfo.getAppName(), serviceInfo.getEnvName(), serviceInfo.getServiceName());
        List<ResourceLifeCycleHandler> handlerList = ResourceHandlers.getHandlersList();
        if (handlerList == null || handlerList.isEmpty()) {
            logger.error("Error while fetching resource lifecycle handler ");
            throw new HyscaleException(TroubleshootErrorCodes.ERROR_WHILE_BUILDING_RESOURCES);
        }

        Map<String, List<Object>> resourceMap = new HashMap<>();
        handlerList.stream().forEach(each -> {
            List resourceList = null;
            try {
                resourceList = each.getBySelector(apiClient, selector, true, namespace);
            } catch (HyscaleException e) {
                logger.debug("Error while fetching resource {} in namespace {} of selector {}", each.getKind(), namespace, selector, e);
            }

            if (resourceList != null && !resourceList.isEmpty()) {
                resourceMap.put(each.getKind(), resourceList);
            }
        });
        return resourceMap;

    }

    private Map<String, List<V1Event>> getResourceEvents(ApiClient apiClient, String namespace) throws HyscaleException {
        V1EventHandler eventHandler = (V1EventHandler) ResourceHandlers.getHandlerOf(ResourceKind.EVENT.getKind());
        Map<String, List<V1Event>> resourceEvents = new HashMap<String, List<V1Event>>();
        try {
            List<V1Event> events = eventHandler.getBySelector(apiClient, null, false, namespace);
            if (events == null || events.isEmpty()) {
                // TODO throw error message to user saying that results may not be appropriate
                return null;
            }

            events.stream().forEach(each -> {
                List<V1Event> eventsList = resourceEvents.get(each.getKind());
                if (eventsList == null) {
                    eventsList = new ArrayList<V1Event>();
                }
                eventsList.add(each);
                resourceEvents.put(each.getKind(), eventsList);
            });
        } catch (HyscaleException e) {
            logger.error("Error while fetching resource events for troubleshooting in namespace {}", namespace);
            throw e;
        }
        return resourceEvents;
    }

}
