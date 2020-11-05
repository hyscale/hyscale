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
package io.hyscale.deployer.services.client;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.deployer.services.model.CustomListObject;
import io.hyscale.deployer.services.model.CustomObject;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import org.bouncycastle.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class GenericK8sClient {

    private static final Logger logger = LoggerFactory.getLogger(GenericK8sClient.class);

    private ApiClient apiClient;
    protected String namespace;
    protected GenericKubernetesApi<CustomObject, CustomListObject> genericClient;

    public GenericK8sClient(ApiClient apiClient){
        this.apiClient = apiClient;
        this.namespace = K8SRuntimeConstants.DEFAULT_NAMESPACE;
    }

    public GenericK8sClient withNamespace(String namespace){
        this.namespace = namespace;
        return this;
    }

    public GenericK8sClient forKind(CustomObject resource){
        String kind = resource.getKind();
        String apiVersion = resource.getApiVersion();

        this.genericClient = new GenericKubernetesApi<CustomObject, CustomListObject>(
                CustomObject.class, CustomListObject.class, getApiGroup(apiVersion),
                getApiVersion(apiVersion),
                kind.toLowerCase() + "s",apiClient);

        return this;
    }

    public abstract void create(CustomObject resource) throws HyscaleException;

    public abstract void update(CustomObject resource) throws HyscaleException;

    public abstract void patch(CustomObject resource) throws HyscaleException;

    public abstract void delete(CustomObject resource);

    public abstract CustomObject get(CustomObject resource);

    private String getApiGroup(String apiVersion) {
        if (apiVersion == null || apiVersion == "") {
            return apiVersion;
        }
        String[] groups = Strings.split(apiVersion, '/');
        return groups.length == 2 ? groups[0] : "";
    }

    private static String getApiVersion(String apiVersion) {
        if (apiVersion == null || apiVersion == "") {
            return apiVersion;
        }
        String[] groups = Strings.split(apiVersion, '/');
        return groups.length == 2 ? groups[1] : groups[0];
    }
}
