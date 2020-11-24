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
package io.hyscale.deployer.services.model;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

import java.util.HashMap;
import java.util.Map;

/**
 *  CustomObject is an implementation of KubernetesObject carrying
 *  context of any given k8s resource kind in a HashMap.
 */

public class CustomObject extends HashMap<String, Object> implements KubernetesObject {

    @Override
    public V1ObjectMeta getMetadata() {
        Map<String,Object> metaMap = (Map) get("metadata");
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName((String) metaMap.get("name"));
        v1ObjectMeta.setNamespace((String) metaMap.get("namespace"));
        v1ObjectMeta.setAnnotations((Map) metaMap.get("annotations"));
        return v1ObjectMeta;
    }

    @Override
    public String getApiVersion() {
        return (String) get("apiVersion");
    }

    @Override
    public String getKind() {
        return (String) get("kind");
    }

}
