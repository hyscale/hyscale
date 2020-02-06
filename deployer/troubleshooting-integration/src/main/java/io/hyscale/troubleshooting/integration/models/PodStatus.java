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
package io.hyscale.troubleshooting.integration.models;

import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.troubleshooting.integration.actions.DefaultAction;
import io.hyscale.troubleshooting.integration.actions.ImagePullBackOffAction;
import io.hyscale.troubleshooting.integration.actions.OOMKilledAction;
import io.hyscale.troubleshooting.integration.conditions.ArePodsReady;
import io.hyscale.troubleshooting.integration.conditions.ContainerStartCommandCheck;
import io.hyscale.troubleshooting.integration.conditions.IsClusterFull;

import java.util.HashMap;
import java.util.Map;

public enum PodStatus {

    IMAGEPULL_BACKOFF("ImagePullBackOff", true) {
        @Override
        public Class<? extends Node> getNextNode() {
            return ImagePullBackOffAction.class;
        }
    },
    CRASHLOOP_BACKOFF("CrashLoopBackOff", true) {
        @Override
        public Class<? extends Node> getNextNode() {
            return ContainerStartCommandCheck.class;
        }
    },
    PENDING("Pending", true) {
        @Override
        public Class<? extends Node> getNextNode() {
            return IsClusterFull.class;
        }
    },
    ERR_IMAGE_PULL("ErrImagePull", true) {
        @Override
        public Class<? extends Node> getNextNode() {
            return ImagePullBackOffAction.class;
        }
    },
    OOMKILLED("OOMKilled", true) {
        @Override
        public Class<? extends Node> getNextNode() {
            return OOMKilledAction.class;
        }
    },
    /*CONTAINER_CREATING_CONFIGERROR("CreateContainerConfigError", true) {
        @Override
        public Class<? extends Node> getNextNode() {
            return null;
        }
    },*/
   /* RUN_CONTAINER_ERROR("RunContainerErr", true) {
        @Override
        public Class<? extends Node> getNextNode() {
            return PodStatus.DEFAULT.getNextNode();
        }
    },*/
    RUNNING("Running", false) {
        @Override
        public Class<? extends Node> getNextNode() {
            return ArePodsReady.class;
        }
    },
    /*  COMPLETED("Completed", true) {
          @Override
          public Class<? extends Node> getNextNode() {
              return null;
          }
      },*/
    /*TERMINATING("Terminating", true) {
        @Override
        public Class<? extends Node> getNextNode() {
            return null;
        }
    },*/
    DEFAULT("default", true) {
        @Override
        public Class<? extends Node> getNextNode() {
            return DefaultAction.class;
        }
    };

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

    public static PodStatus get(String status) {
        return podStatusMap.getOrDefault(status, PodStatus.DEFAULT);
    }

    public abstract Class<? extends Node> getNextNode();

    public boolean isFailed() {
        return failed;
    }
}
