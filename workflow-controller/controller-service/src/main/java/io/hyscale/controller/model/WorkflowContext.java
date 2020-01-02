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
package io.hyscale.controller.model;

import io.hyscale.commons.component.ComponentContext;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

import java.util.HashMap;
import java.util.Map;

/**
 * Context information for workflow controller
 *
 */
public class WorkflowContext extends ComponentContext {

    private ServiceSpec serviceSpec;
    private String namespace;

    private Map<String, Object> attributes;

    public WorkflowContext() {
        attributes = new HashMap<>();
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public ServiceSpec getServiceSpec() {
        return serviceSpec;
    }

    public void setServiceSpec(ServiceSpec serviceSpec) {
        this.serviceSpec = serviceSpec;
    }


}
