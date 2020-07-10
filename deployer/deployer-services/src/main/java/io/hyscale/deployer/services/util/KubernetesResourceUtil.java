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

import io.hyscale.commons.models.KubernetesResource;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.YAMLManifest;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Yaml;

/**
 * Utility for generic kubernetes resource
 *
 */
public class KubernetesResourceUtil {

    private static final String GET_KIND = "getKind";
    private static final String GET_METADATA = "getMetadata";
    
    private KubernetesResourceUtil() {}

    public static KubernetesResource getKubernetesResource(Manifest manifest, String namespace)
            throws NoSuchMethodException, IOException, IllegalAccessException,
            InvocationTargetException {
        if (manifest == null) {
            return null;
        }
        KubernetesResource resource = new KubernetesResource();
        YAMLManifest yamlManifest = (YAMLManifest) manifest;
        Object obj = Yaml.load(yamlManifest.getManifest());
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

    public static V1ObjectMeta getObjectMeta(Object object)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (object == null) {
            return null;
        }
        Method metadataMethod = object.getClass().getMethod(GET_METADATA);

        return (V1ObjectMeta) metadataMethod.invoke(object);
    }

}
