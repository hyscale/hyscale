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
package io.hyscale.deployer.services.builder;

import io.hyscale.deployer.services.model.Container;
import io.hyscale.deployer.services.model.Pod;
import io.hyscale.deployer.services.model.Volume;

import java.util.List;

public class PodBuilder {

    private Pod pod;

    public PodBuilder() {
        pod = new Pod();
    }

    public PodBuilder withName(String name) {
        pod.setName(name);
        return this;
    }

    public PodBuilder withStatus(String status) {
        pod.setStatus(status);
        return this;
    }

    public PodBuilder withReady(boolean isReady) {
        pod.setReady(isReady);
        return this;
    }

    public PodBuilder withContainers(List<Container> containerList) {
        pod.setContainers(containerList);
        return this;
    }

    public PodBuilder withVolumes(List<Volume> volumeList) {
        pod.setVolumes(volumeList);
        return this;
    }

    public Pod get() {
        return pod;
    }

}
