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
package io.hyscale.deployer.core.model;

import org.apache.commons.lang3.StringUtils;

public enum ResourceKind {

    POD("Pod",0,"v1"),
    STATEFUL_SET("StatefulSet", 1, "apps/v1"),
    DEPLOYMENT("Deployment", 1,"apps/v1"),
    CONFIG_MAP("ConfigMap", 0,"v1"),
    REPLICA_SET("ReplicaSet",0,"apps/v1"),
    SECRET("Secret", 0,"v1"),
    SERVICE("Service", 0, "v1"),
    NAMESPACE("Namespace",0,"v1"),
    STORAGE_CLASS("StorageClass",0,"storage.k8s.io/v1"),
    HORIZONTAL_POD_AUTOSCALER("HorizontalPodAutoscaler", 2),
    PERSISTENT_VOLUME_CLAIM("PersistentVolumeClaim", 2,"v1"),
    EVENT("Event"),
    VERSION("VersionInfo"),
    JOB("Job",0,"batch/v1");

    private String kind;

    /**
     *  Deletion and creation order
     */
    private int weight = 0;
    private String apiVersion;

    ResourceKind(String kind) {
        this.kind = kind;
    }

    ResourceKind(String kind, int weight) {
        this.kind = kind;
        this.weight = weight;
    }

    ResourceKind(String kind, int weight,String apiVersion){
        this.kind = kind;
        this.weight = weight;
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return this.kind;
    }

    public int getWeight() {
        return this.weight;
    }

    public String getApiVersion() { return this.apiVersion; }

    public static ResourceKind fromString(String kind) {
        if (StringUtils.isBlank(kind)) {
            return null;
        }
        for (ResourceKind resourceKind : ResourceKind.values()) {
            String res = resourceKind.getKind();
            res.isBlank();
            if (res.equalsIgnoreCase(kind)) {
                return resourceKind;
            }
        }
        return null;
    }
}
