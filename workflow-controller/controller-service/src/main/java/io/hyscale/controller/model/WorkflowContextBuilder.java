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


import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.AuthConfig;

public class WorkflowContextBuilder {

    private WorkflowContext workflowContext;

    public WorkflowContextBuilder(String appName) {
        this.workflowContext = new WorkflowContext(appName);
    }

    public WorkflowContextBuilder withNamespace(String namespace) {
        this.workflowContext.setNamespace(namespace);
        return this;
    }

    public WorkflowContextBuilder withProfile(String profile) {
        this.workflowContext.setEnvName(profile);
        return this;
    }

    public WorkflowContextBuilder withService(ServiceSpec serviceSpec) throws HyscaleException {
        if (serviceSpec != null) {
            this.workflowContext.setServiceSpec(serviceSpec);
            this.workflowContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        }
        return this;
    }

    public WorkflowContextBuilder withAuthConfig(AuthConfig authConfig) {
        this.workflowContext.setAuthConfig(authConfig);
        return this;
    }

    public WorkflowContextBuilder withServiceName(String serviceName) {
        this.workflowContext.setServiceName(serviceName);
        return this;
    }

    public WorkflowContext get() {
        return this.workflowContext;
    }

}
