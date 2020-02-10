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

import java.util.HashMap;
import java.util.Map;

//TODO JAVADOC
public enum PodStatus {

    IMAGEPULL_BACKOFF("ImagePullBackOff", true),
    CRASHLOOP_BACKOFF("CrashLoopBackOff", true),
    PENDING("Pending", true),
    ERR_IMAGE_PULL("ErrImagePull", true),
    OOMKILLED("OOMKilled", true),
    RUN_CONTAINER_ERROR("RunContainerErr", true),
    RUNNING("Running", false),
    DEFAULT("default", true);

    private String status;
    private boolean failed;
    private static Map<String, PodStatus> podStatusMap = new HashMap<>();

    PodStatus(String status, boolean failed) {
        this.status = status;
        this.failed = failed;
    }

    static {
        for (PodStatus each : PodStatus.values()) {
            podStatusMap.put(each.getStatus(), each);
        }
    }

    public String getStatus() {
        return status;
    }

    public boolean isFailed() {
        return failed;
    }

    public static PodStatus get(String status) {
        return podStatusMap.getOrDefault(status, PodStatus.DEFAULT);
    }
}
