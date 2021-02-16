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
package io.hyscale.controller.validator.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.StructuredOutputHandler;
import io.hyscale.commons.logger.LoggerTags;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.activity.ValidatorActivity;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.provider.PortsProvider;
import io.hyscale.generator.services.builder.DefaultPortsBuilder;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.NetworkTrafficRule;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * This class covers different cases
 * for validity of network traffic rules
 * before they can be applied.
 */


@Component
public class NetworkPoliciesValidator implements Validator<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(NetworkPoliciesValidator.class);

    @Autowired
    private StructuredOutputHandler structuredOutputHandler;

    @Autowired
    private PortsProvider portsProvider;

    @Autowired
    DefaultPortsBuilder defaultPortsBuilder;

    @Override
    public boolean validate(WorkflowContext workflowContext) throws HyscaleException {
        logger.info("Validating Network Policies");
        ServiceSpec serviceSpec = workflowContext.getServiceSpec();
        boolean external = BooleanUtils.toBoolean(serviceSpec.get(HyscaleSpecFields.external, boolean.class));
        List<NetworkTrafficRule> networkTrafficRules = serviceSpec.get(HyscaleSpecFields.allowTraffic, new TypeReference<>() {
        });

        if (external && networkTrafficRules != null) {
            logger.info("External Cannot be True to Apply Network Traffic Rules");
            addErrorMessage(ValidatorActivity.INVALID_EXTERNAL_VALUE);
            WorkflowLogger.persist(ValidatorActivity.INVALID_EXTERNAL_VALUE, LoggerTags.ERROR);
            return false;
        }
        if (external || CollectionUtils.isEmpty(networkTrafficRules)) {
            return true;
        }
        return validateTrafficRules(networkTrafficRules, portsProvider.getExposedPorts(serviceSpec, true));
    }

    //Check for Valid Network Traffic Rules
    private boolean validateTrafficRules(List<NetworkTrafficRule> networkTrafficRules, List<String> exposedPorts) {
        StringBuilder portsNotExposed = new StringBuilder();
        boolean portsValid = true;
        for (NetworkTrafficRule networkTrafficRule : networkTrafficRules) {
            if (networkTrafficRule.getPorts() == null) {
                logger.info("Network traffic Rules are not Valid");
                addErrorMessage(ValidatorActivity.MISSING_PORTS, (String) null);
                WorkflowLogger.persist(ValidatorActivity.MISSING_PORTS, LoggerTags.ERROR);
                return false;
            }
            // Rules are invalid if ports are not exposed
            for (String port : networkTrafficRule.getPorts()) {
                port = defaultPortsBuilder.updatePortProtocol(port);
                if (CollectionUtils.isEmpty(exposedPorts) || !exposedPorts.contains(port)) {
                    portsNotExposed.append(port + " ");
                    portsValid = false;
                }
            }
        }
        if (!portsValid) {
            logger.info("Cannot apply traffic rules to ports that are not exposed");
            addErrorMessage(ValidatorActivity.PORT_NOT_EXPOSED, portsNotExposed.toString());
            WorkflowLogger.persist(ValidatorActivity.PORT_NOT_EXPOSED, LoggerTags.ERROR, portsNotExposed.toString());
            return false;
        }
        return true;
    }

    private void addErrorMessage(ValidatorActivity validatorActivity, String... args) {
        if (WorkflowLogger.isDisabled()) {
            structuredOutputHandler.addErrorMessage(validatorActivity.getActivityMessage(), args);
        }
    }
}
