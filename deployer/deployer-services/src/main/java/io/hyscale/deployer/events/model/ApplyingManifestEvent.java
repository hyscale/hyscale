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

import java.util.List;

import io.hyscale.commons.framework.events.model.ActivityEvent;
import io.hyscale.commons.framework.events.model.ActivityState;
import io.hyscale.commons.models.Manifest;

public class ApplyingManifestEvent extends ActivityEvent {
    private List<Manifest> manifests;
    private String namespace;
    
    public ApplyingManifestEvent(ActivityState state) {
        super(state);
    }

    public ApplyingManifestEvent(ActivityState state, List<Manifest> manifests, String namespace) {
        super(state);
        this.manifests = manifests;
        this.namespace = namespace;
    }

    public List<Manifest> getManifests() {
        return manifests;
    }

    public String getNamespace() {
        return namespace;
    }

}
