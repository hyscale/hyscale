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
package io.hyscale.controller.builder;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.controller.model.EffectiveServiceSpec;
import io.hyscale.controller.model.WorkflowContext;

@Component
public class WorkflowContextBuilder {

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    public List<WorkflowContext> buildContextList(String appName, String namespace, List<String> serviceList) {
        List<WorkflowContext> contextList = new ArrayList<WorkflowContext>();
        if (serviceList == null) {
            contextList.add(buildContext(appName, namespace, null));
            return contextList;
        }
        serviceList.forEach(serviceName -> contextList.add(buildContext(appName, namespace, serviceName)));
        return contextList;
    }

    public WorkflowContext buildContext(String appName, String namespace, String serviceName) {
        WorkflowContext context = new WorkflowContext();
        context.setAppName(appName);
        context.setNamespace(namespace);
        context.setServiceName(serviceName);
        return context;
    }

    public List<WorkflowContext> buildContextList(List<EffectiveServiceSpec> effectiveServiceSpecList, String appName,
            String namespace) {
        List<WorkflowContext> contextList = new ArrayList<WorkflowContext>();
        effectiveServiceSpecList.forEach(each -> {
            contextList.add(buildContext(each, appName, namespace));
        });
        return contextList;
    }

    public WorkflowContext buildContext(EffectiveServiceSpec effectiveServiceSpec, String appName, String namespace) {
        if (effectiveServiceSpec == null) {
            return null;
        }
        WorkflowContext context = new WorkflowContext();
        context.setAppName(appName);
        context.setNamespace(namespace);
        context.setServiceSpec(effectiveServiceSpec.getServiceSpec());
        context.setServiceName(effectiveServiceSpec.getServiceMetadata().getServiceName());
        context.setEnvName(effectiveServiceSpec.getServiceMetadata().getEnvName());
        return context;
    }

    public List<WorkflowContext> updateAuthConfig(List<WorkflowContext> contextList) throws HyscaleException {
        if (contextList == null) {
            return null;
        }
        AuthConfig authConfig = authConfigBuilder.getAuthConfig();
        contextList.forEach(each -> each.setAuthConfig(authConfig));
        return contextList;
    }

    public WorkflowContext updateAuthConfig(WorkflowContext context, String kubeConfigPath) throws HyscaleException {
        if (context == null) {
            return context;
        }
        context.setAuthConfig(authConfigBuilder.getAuthConfig(kubeConfigPath));
        return context;
    }

    public WorkflowContext updateAuthConfig(WorkflowContext context) throws HyscaleException {
        if (context == null) {
            return context;
        }
        context.setAuthConfig(authConfigBuilder.getAuthConfig());
        return context;
    }

}
