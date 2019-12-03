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
package io.hyscale.deployer.services.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.KubernetesResource;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.YAMLManifest;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.util.Yaml;

/**
 * Utility for generic kubernetes resource
 *
 */
public class KubernetesResourceUtil {

    private static final String GET_KIND = "getKind";
    private static final String GET_METADATA = "getMetadata";

    public static KubernetesResource getKubernetesResource(Manifest manifest, String namespace)
            throws NoSuchMethodException, SecurityException, IOException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        if (manifest == null) {
            return null;
        }

        KubernetesResource resource = new KubernetesResource();
        YAMLManifest yamlManifest = (YAMLManifest) manifest;
        Object obj = Yaml.load(yamlManifest.getYamlManifest());
        Method kindMethod = obj.getClass().getMethod(GET_KIND);
        String kind = (String) kindMethod.invoke(obj);

        V1ObjectMeta v1ObjectMeta = getObjectMeta(obj);
        if (v1ObjectMeta != null) {
            v1ObjectMeta.setNamespace(namespace);
        }
        resource.setV1ObjectMeta(v1ObjectMeta);
        resource.setKind(kind);
        resource.setResource(obj);
        return resource;

    }

    public static V1ObjectMeta getObjectMeta(Object object) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (object == null) {
            return null;
        }
        Method metadataMethod = object.getClass().getMethod(GET_METADATA);

        V1ObjectMeta v1ObjectMeta = (V1ObjectMeta) metadataMethod.invoke(object);

        return v1ObjectMeta;
    }

    public static boolean isResourceMetadataValid(V1ObjectMeta metadata) {
        if (metadata == null || StringUtils.isBlank(metadata.getName())) {
            return false;
        }
        return true;
    }

    public static boolean isResourceValid(ApiClient apiClient, String kind, V1ObjectMeta metadata, String name)
            throws HyscaleException {
        return isResourceValid(apiClient, kind, metadata) && isResourceValid(apiClient, kind, name);
    }

    public static boolean isResourceValid(ApiClient apiClient, String kind, V1ObjectMeta metadata)
            throws HyscaleException {
        if (apiClient == null) {
            throw new HyscaleException(DeployerErrorCodes.API_CLIENT_REQUIRED);
        }
        if (!isResourceMetadataValid(metadata)) {
            throw new HyscaleException(DeployerErrorCodes.INVALID_RESOURCE_DATA, kind);
        }
        return true;
    }

    public static boolean isResourceValid(ApiClient apiClient, String kind, String name) throws HyscaleException {
        if (StringUtils.isBlank(name)) {
            throw new HyscaleException(DeployerErrorCodes.INVALID_RESOURCE_NAME, kind);
        }

        if (apiClient == null) {
            throw new HyscaleException(DeployerErrorCodes.API_CLIENT_REQUIRED);
        }
        return true;
    }

}
