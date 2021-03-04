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

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import io.hyscale.commons.models.KubernetesResource;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.YAMLManifest;
import io.hyscale.deployer.services.model.CustomObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.constructor.ConstructorException;

/**
 * Utility for generic kubernetes resource
 *
 */
public class KubernetesResourceUtil {

    private static final String GET_KIND = "getKind";
    private static final String GET_METADATA = "getMetadata";
    private static final Logger logger = LoggerFactory.getLogger(KubernetesResourceUtil.class);
    
    private KubernetesResourceUtil() {}

    public static KubernetesResource getKubernetesResource(Manifest manifest, String namespace)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        if (manifest == null) {
            return null;
        }
        KubernetesResource resource = new KubernetesResource();
        YAMLManifest yamlManifest = (YAMLManifest) manifest;
        Object obj = null;
        try{
            obj = Yaml.load(yamlManifest.getManifest());
        }catch (ConstructorException | IOException e){
            logger.error("Failed to load manifest returning null");
            return null;
        }
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

    public static CustomObject getK8sCustomObjectResource(Manifest manifest, String namespace)
            throws IOException {
        if (manifest == null) {
                return null;
            }
        YAMLManifest yamlManifest = (YAMLManifest) manifest;
        Map<String, Object> data = (Map) Yaml.getSnakeYaml().load((Reader)(new FileReader(yamlManifest.getManifest())));
        CustomObject customObject = new CustomObject();
        customObject.putAll(data);
        Map<String,Object> metaMap = (Map) customObject.get("metadata");
        if(metaMap.get("namespace")==null && namespace!=null){
            // Adding namespace in metadata if not provided
            metaMap.put("namespace",namespace);
        }
        return customObject;
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
