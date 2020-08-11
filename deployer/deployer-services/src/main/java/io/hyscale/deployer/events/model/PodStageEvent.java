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
package io.hyscale.deployer.events.model;

import io.hyscale.commons.framework.events.model.HyscaleEvent;
import io.hyscale.commons.models.ServiceMetadata;

public class PodStageEvent extends HyscaleEvent {

    private ServiceMetadata serviceMetadata;
    private String namespace;
    private PodStage stage;
    private String status;
    private boolean failure;

    public PodStageEvent(ServiceMetadata serviceMetadata, String namespace, PodStage stage) {
        super(stage);
        this.serviceMetadata = serviceMetadata;
        this.namespace = namespace;
        this.stage = stage;
    }

    public PodStageEvent(ServiceMetadata serviceMetadata, String namespace, PodStage stage, String status) {
        super(stage);
        this.serviceMetadata = serviceMetadata;
        this.namespace = namespace;
        this.stage = stage;
        this.status = status;
    }

    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    public String getNamespace() {
        return namespace;
    }

    public PodStage getStage() {
        return stage;
    }

    public boolean isFailure() {
        return failure;
    }

    public void setFailure(boolean failure) {
        this.failure = failure;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public enum PodStage {
        INITIALIZATION, CREATION, READINESS
    }
}
