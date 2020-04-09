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
package io.hyscale.controller.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.model.EffectiveServiceSpec;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.validator.impl.ClusterValidator;
import io.hyscale.controller.validator.impl.DockerValidator;
import io.hyscale.controller.validator.impl.ManifestValidator;
import io.hyscale.controller.validator.impl.RegistryValidator;
import io.hyscale.controller.validator.impl.VolumeValidator;

@Component
public class ManifestAndDeployHelper {
    
    @Autowired
    private DockerValidator dockerValidator;
    
    @Autowired
    private RegistryValidator registryValidator;
    
    @Autowired
    private ManifestValidator manifestValidator;
    
    @Autowired
    private ClusterValidator clusterValidator;
    
    @Autowired
    private VolumeValidator volumeValidator;
    
    private List<Validator<WorkflowContext>> generateManifestPostValidators = new ArrayList<Validator<WorkflowContext>>();
    
    private List<Validator<WorkflowContext>> deployPostValidators = new ArrayList<Validator<WorkflowContext>>();
    
    @PostConstruct
    public void init() {
        // Post validators
        deployPostValidators.add(dockerValidator);
        deployPostValidators.add(registryValidator);
        deployPostValidators.add(manifestValidator);
        deployPostValidators.add(clusterValidator);
        deployPostValidators.add(volumeValidator);
        
        generateManifestPostValidators.add(manifestValidator);
    }
    
    public List<Validator<WorkflowContext>> getManifestPostValidators() {
        return generateManifestPostValidators;
    }
    
    public List<Validator<WorkflowContext>> getDeployPostValidators() {
        return deployPostValidators;
    }

    public List<WorkflowContext> getContextList(List<EffectiveServiceSpec> effectiveServiceSpecList,
            String appName, String namespace) {
        List<WorkflowContext> contextList = new ArrayList<WorkflowContext>();
        effectiveServiceSpecList.forEach(each -> {
            WorkflowContext context = new WorkflowContext();
            context.setAppName(appName);
            context.setNamespace(namespace);
            context.setServiceSpec(each.getServiceSpec());
            context.setServiceName(each.getServiceMetadata().getServiceName());
            contextList.add(context);
        });
        return contextList;
    }

}