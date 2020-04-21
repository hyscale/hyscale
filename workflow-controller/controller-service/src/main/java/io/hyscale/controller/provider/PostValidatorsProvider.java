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
package io.hyscale.controller.provider;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.validator.impl.ClusterValidator;
import io.hyscale.controller.validator.impl.DockerDaemonValidator;
import io.hyscale.controller.validator.impl.ManifestValidator;
import io.hyscale.controller.validator.impl.RegistryValidator;
import io.hyscale.controller.validator.impl.VolumeValidator;

/**
 * Provides post processing validators
 * @author tushar
 *
 */
@Component
public class PostValidatorsProvider {

    @Autowired
    private DockerDaemonValidator dockerValidator;

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

        generateManifestPostValidators.add(registryValidator);
        generateManifestPostValidators.add(manifestValidator);
    }

    public List<Validator<WorkflowContext>> getManifestPostValidators() {
        return generateManifestPostValidators;
    }

    public List<Validator<WorkflowContext>> getDeployPostValidators() {
        return deployPostValidators;
    }

}
