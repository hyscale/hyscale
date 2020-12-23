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
package io.hyscale.deployer.services.broker;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.KubernetesResource;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.model.ResourceUpdatePolicy;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class K8sResourceBroker {

    private static final Logger logger = LoggerFactory.getLogger(K8sResourceBroker.class);

    private ApiClient apiClient;
    private String namespace;

    public K8sResourceBroker(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void withNamespace(String namespace) {
        this.namespace = namespace;
    }

    public <T> T get(ResourceLifeCycleHandler<T> lifeCycleHandler, String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        try {
            return lifeCycleHandler.get(apiClient, name, namespace);
        } catch (HyscaleException e) {
            logger.debug("Error while fetching resource {} ", lifeCycleHandler.getKind(), e);
        }
        return null;
    }

    /**
     * Handle resource update based on update policy
     */
    public <T> void update(ResourceLifeCycleHandler<T> lifeCycleHandler, KubernetesResource kubernetesResource, ResourceUpdatePolicy updatePolicy) throws HyscaleException {
        if (kubernetesResource == null || kubernetesResource.getV1ObjectMeta() == null) {
            return;
        }
        V1ObjectMeta objectMeta = kubernetesResource.getV1ObjectMeta();
        T obj = (T) kubernetesResource.getResource();
        String resourceNamespace = kubernetesResource.getV1ObjectMeta().getNamespace();
        switch (updatePolicy) {
            case PATCH:
                try {
                    boolean isPatched = lifeCycleHandler.patch(apiClient, objectMeta.getName(), resourceNamespace, obj);
                    if (!isPatched) {
                        // Fallback to delete and create if patch fails to apply
                        lifeCycleHandler.delete(apiClient, objectMeta.getName(), resourceNamespace, true);
                        lifeCycleHandler.create(apiClient, obj, resourceNamespace);

                    }
                } catch (HyscaleException e) {
                    // Fallback to delete and create if patch fails to apply
                    lifeCycleHandler.delete(apiClient, objectMeta.getName(), resourceNamespace, true);
                    lifeCycleHandler.create(apiClient, obj, resourceNamespace);
                }
                break;
            case UPDATE:
                lifeCycleHandler.update(apiClient, obj, resourceNamespace);
                break;
            case DELETE_AND_CREATE:
                lifeCycleHandler.delete(apiClient, objectMeta.getName(), resourceNamespace, true);
                lifeCycleHandler.create(apiClient, obj, resourceNamespace);
                break;
        }
    }

    public <T> void create(ResourceLifeCycleHandler<T> lifeCycleHandler, T resource) {
        if (resource == null) {
            return;
        }
        try {
            lifeCycleHandler.create(apiClient, resource, namespace);
        } catch (HyscaleException e) {
            logger.debug("Error while creating resource {}", lifeCycleHandler.getKind(), e);
        }
    }


}
