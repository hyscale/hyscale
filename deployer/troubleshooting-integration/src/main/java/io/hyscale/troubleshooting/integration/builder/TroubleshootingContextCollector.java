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
import io.hyscale.commons.models.ResourceFieldSelectorKey;
import io.hyscale.commons.utils.FieldSelectorUtil;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.handler.impl.V1EventHandler;
import io.hyscale.deployer.services.handler.impl.V1StorageClassHandler;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.troubleshooting.integration.errors.TroubleshootErrorCodes;
import io.hyscale.troubleshooting.integration.models.ServiceInfo;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1StorageClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


//TODO JAVADOC
// Exception Handling
@Component
public class TroubleshootingContextCollector {

    private static final Logger logger = LoggerFactory.getLogger(TroubleshootingContextCollector.class);

    @Autowired
    private K8sClientProvider k8sClientProvider;

    public TroubleshootingContext build(@NonNull ServiceInfo serviceInfo, @NonNull K8sAuthorisation k8sAuthorisation, @NonNull String namespace) throws HyscaleException {
        TroubleshootingContext context = new TroubleshootingContext();
        try {
            ApiClient apiClient = k8sClientProvider.get(k8sAuthorisation);

            context.setServiceInfo(serviceInfo);
            context.setTrace(true);
            long start = System.currentTimeMillis();
            context.setResourceInfos(getResources(serviceInfo, apiClient, namespace));
            System.out.println(TimeUnit.SECONDS.convert(System.currentTimeMillis() - start, TimeUnit.MILLISECONDS));
        } catch (HyscaleException e) {
            logger.error("Error while preparing context to troubleshoot the service {}", serviceInfo.getServiceName());
            throw e;
        }
        return context;
    }

    private Map<String, List<TroubleshootingContext.ResourceInfo>> getResources(@NonNull ServiceInfo serviceInfo, @NonNull ApiClient apiClient, @NonNull String namespace) throws HyscaleException {
        String selector = ResourceSelectorUtil.getSelector(serviceInfo.getAppName(), serviceInfo.getEnvName(), serviceInfo.getServiceName());
        List<ResourceLifeCycleHandler> handlerList = ResourceHandlers.getHandlersList();
        if (handlerList == null || handlerList.isEmpty()) {
            logger.error("Error while fetching resource lifecycle handler ");
            throw new HyscaleException(TroubleshootErrorCodes.ERROR_WHILE_BUILDING_RESOURCES);
        }

        V1EventHandler eventHandler = (V1EventHandler) ResourceHandlers.getHandlerOf(ResourceKind.EVENT.getKind());
        Map<String, List<TroubleshootingContext.ResourceInfo>> resourceMap = new HashMap<>();
        handlerList.stream().forEach(each -> {
            List resourceList = null;
            try {
                resourceList = each.getBySelector(apiClient, selector, true, namespace);
            } catch (HyscaleException e) {
                logger.debug("Error while fetching resource {} in namespace {} of selector {}", each.getKind(), namespace, selector, e);
            }

            // Construct resourceInfo for each resource of this kind
            if (resourceList != null && !resourceList.isEmpty()) {
                List<TroubleshootingContext.ResourceInfo> resourceInfoList = new ArrayList<>();
                resourceList.stream().forEach(eachResource -> {
                    TroubleshootingContext.ResourceInfo resourceInfo = new TroubleshootingContext.ResourceInfo();
                    V1ObjectMeta v1ObjectMeta = (V1ObjectMeta) eachResource;
                    resourceInfo.setResource(eachResource);
                    try {
                        resourceInfo.setEvents(eventHandler.getBySelector(apiClient, getFieldSelector(v1ObjectMeta.getName(), namespace), false, namespace));
                    } catch (HyscaleException e) {
                        logger.debug("Error while fetching resource {} logs in namespace {}", eachResource.getClass(), namespace);
                    }
                    resourceInfoList.add(resourceInfo);
                });

                if (!resourceInfoList.isEmpty()) {
                    resourceMap.put(each.getKind(), resourceInfoList);
                }
            }
        });

        V1StorageClassHandler storageClassHandler = (V1StorageClassHandler) ResourceHandlers.getHandlerOf(ResourceKind.STORAGE_CLASS.getKind());
        List<V1StorageClass> storageClasses = storageClassHandler.getAll(apiClient);
        if (storageClasses != null && !storageClasses.isEmpty()) {
            List<TroubleshootingContext.ResourceInfo> storageClassResourceInfoList =
                    storageClasses.stream().map(each -> {
                        TroubleshootingContext.ResourceInfo resourceInfo = new TroubleshootingContext.ResourceInfo();
                        resourceInfo.setResource(each);
                        return resourceInfo;
                    }).collect(Collectors.toList());
            if (storageClassResourceInfoList != null && !storageClassResourceInfoList.isEmpty()) {
                resourceMap.put(ResourceKind.STORAGE_CLASS.getKind(), storageClassResourceInfoList);
            }
        }


        return resourceMap;
    }

    private String getFieldSelector(String name, String namespace) {
        Map<ResourceFieldSelectorKey, String> fieldMap = new HashMap<>();
        fieldMap.put(V1EventHandler.EventFieldKey.INVOLVED_OBJECT_NAME, name);
        fieldMap.put(V1EventHandler.EventFieldKey.INVOLVED_OBJECT_NAMESPACE, namespace);
        return FieldSelectorUtil.getSelectorFromFieldMap(fieldMap);
    }
}
