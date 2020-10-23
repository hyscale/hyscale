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

import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.models.V1ListMeta;

import java.util.List;

public class CustomListObject implements KubernetesListObject {

    private String kind;
    private String apiVersion;
    private V1ListMeta v1ListMeta;
    private List<CustomObject> customObjectList;

    public CustomListObject(String kind,String apiVersion){
        this.kind = kind;
        this.apiVersion = apiVersion;
    }

    @Override
    public V1ListMeta getMetadata() {
        return v1ListMeta;
    }

    @Override
    public List<CustomObject> getItems() {
        return customObjectList;
    }

    @Override
    public String getApiVersion() {
        return apiVersion;
    }

    @Override
    public String getKind() {
        return kind;
    }

    public List<CustomObject> getCustomObjectList() {
        return customObjectList;
    }

    public void setCustomObjectList(List<CustomObject> customObjectList) {
        this.customObjectList = customObjectList;
    }

    public void addItem(CustomObject customObject){
        this.customObjectList.add(customObject);
    }
}