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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TroubleshootingContext implements NodeContext {

    private ServiceInfo serviceInfo;
    private Map<String, ResourceData> resourceData;
    private Map<String, Object> communicationAttributes;
    private boolean debug;

    public TroubleshootingContext() {
        this.communicationAttributes = new HashMap<>();
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public Map<String, ResourceData> getResourceData() {
        return resourceData;
    }

    public void setResourceData(Map<String, ResourceData> resourceData) {
        this.resourceData = resourceData;
    }

    public Map<String, Object> getCommunicationAttributes() {
        return Collections.unmodifiableMap(communicationAttributes);
    }

    public Object addAttribute(String key, Object value) {
        return communicationAttributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return communicationAttributes.get(key);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public static class ResourceData {

        private List<V1Event> events;
        private List<Object> resource;

        public List<V1Event> getEvents() {
            return events;
        }

        public void setEvents(List<V1Event> events) {
            this.events = events;
        }

        public List<Object> getResource() {
            return resource;
        }

        public void setResource(List<Object> resource) {
            this.resource = resource;
        }
    }
}
