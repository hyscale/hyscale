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

import io.kubernetes.client.models.V1Event;
import io.kubernetes.client.models.V1Pod;

import java.util.List;

public enum FailedResourceKey {

    FAILED_POD(V1Pod.class),
    FAILED_POD_EVENTS(List.class),
    UNREADY_POD(V1Pod.class),
    UNHEALTHY_POD_EVENT(V1Event.class);

    private Class klazz;

    public Class getKlazz() {
        return klazz;
    }

    FailedResourceKey(Class klazz) {
        this.klazz = klazz;
    }
}
