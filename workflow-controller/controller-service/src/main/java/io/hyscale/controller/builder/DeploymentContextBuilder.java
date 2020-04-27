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

import java.util.List;

import org.springframework.stereotype.Component;

import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.Manifest;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;

/**
 * Builder to create {@link DeploymentContext} from {@link WorkflowContext}
 *
 */
@Component
public class DeploymentContextBuilder {

    public DeploymentContext build(WorkflowContext workflowContext) {

        if (workflowContext == null) {
            return null;
        }
        DeploymentContext deploymentContext = new DeploymentContext();
        deploymentContext.setAuthConfig(workflowContext.getAuthConfig());
        deploymentContext.setNamespace(workflowContext.getNamespace());
        deploymentContext.setAppName(workflowContext.getAppName());
        deploymentContext.setServiceName(workflowContext.getServiceName());
        
        Object manifestList = workflowContext.getAttribute(WorkflowConstants.GENERATED_MANIFESTS);
        if (manifestList != null) {
            deploymentContext.setManifests((List<Manifest>) manifestList);
        }

        return deploymentContext;
    }
}
