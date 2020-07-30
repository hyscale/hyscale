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

import io.hyscale.commons.models.ServiceMetadata;
import io.kubernetes.client.openapi.models.V1Event;

import java.util.*;

//TODO JAVADOC
public class TroubleshootingContext implements NodeContext {

    private ServiceMetadata serviceMetadata;
    private Map<String, List<ResourceInfo>> resourceInfos;
    private Map<FailedResourceKey, Object> failedObjects;
    private List<DiagnosisReport> diagnosisReports;
    private boolean trace;

    public TroubleshootingContext() {
        this.failedObjects = new EnumMap<>(FailedResourceKey.class);
        this.diagnosisReports = new ArrayList<>();
    }

    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    public void setServiceMetadata(ServiceMetadata serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }

    public Map<FailedResourceKey, Object> getFailedObjects() {
        return Collections.unmodifiableMap(failedObjects);
    }

    public Object addAttribute(FailedResourceKey key, Object value) {
        return failedObjects.put(key, value);
    }

    public Object getAttribute(FailedResourceKey key) {
        return failedObjects.get(key);
    }

    public boolean isTrace() {
        return trace;
    }

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public Map<String, List<ResourceInfo>> getResourceInfos() {
        return resourceInfos;
    }

    public void setResourceInfos(Map<String, List<ResourceInfo>> resourceInfos) {
        this.resourceInfos = resourceInfos;
    }

    public List<DiagnosisReport> getDiagnosisReports() {
        return Collections.unmodifiableList(diagnosisReports);
    }

    public void addReport(DiagnosisReport report) {
        diagnosisReports.add(report);
    }


    public static class ResourceInfo {

        private List<V1Event> events;
        private Object resource;

        public List<V1Event> getEvents() {
            return events;
        }

        public void setEvents(List<V1Event> events) {
            this.events = events;
        }

        public Object getResource() {
            return resource;
        }

        public void setResource(Object resource) {
            this.resource = resource;
        }
    }


}
