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

    POD("Pod"),
    STATEFUL_SET("StatefulSet", 1),
    DEPLOYMENT("Deployment", 1),
    CONFIG_MAP("ConfigMap", 0),
    REPLICA_SET("ReplicaSet"),
    SECRET("Secret", 0),
    SERVICE("Service", 0),
    NAMESPACE("Namespace"),
    STORAGE_CLASS("StorageClass"),
    HORIZONTAL_POD_AUTOSCALER("HorizontalPodAutoscaler", 0),
    PERSISTENT_VOLUME_CLAIM("PersistentVolumeClaim", 2),
    EVENT("Event");

    private String kind;

    /**
     *  Deletion and creation order
     */
    private int weight = 0;

    ResourceKind(String kind) {
        this.kind = kind;
    }

    ResourceKind(String kind, int weight) {
        this.kind = kind;
        this.weight = weight;
    }

    public String getKind() {
        return this.kind;
    }

    public int getWeight() {
        return this.weight;
    }

    public static ResourceKind fromString(String kind) {
        if (StringUtils.isBlank(kind)) {
            return null;
        }
        for (ResourceKind resourceKind : ResourceKind.values()) {
            if (resourceKind.getKind().equalsIgnoreCase(kind)) {
                return resourceKind;
            }
        }
        return null;
    }
}
