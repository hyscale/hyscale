/**
 * Copyright 2019 Pramati Prism, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.NetworkTrafficRule;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NetworkPoliciesValidator implements Validator<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(NetworkPoliciesValidator.class);

    @Autowired
    private StructuredOutputHandler structuredOutputHandler;

    @Override
    public boolean validate(WorkflowContext workflowContext) throws HyscaleException {
        logger.info("Validating Network Policies");
        ServiceSpec serviceSpec = workflowContext.getServiceSpec();
        boolean external = false;
        if (serviceSpec.get(HyscaleSpecFields.external, boolean.class) != null) {
            external = serviceSpec.get(HyscaleSpecFields.external, boolean.class);
        }
        List<NetworkTrafficRule> networkTrafficRules = serviceSpec.get(HyscaleSpecFields.allowTraffic, new TypeReference<>() {
        });

        if (external && networkTrafficRules != null) {
            logger.info("External Cannot be True to Apply Network Traffic Rules");
            addErrorMessage(ValidatorActivity.INVALID_VALUE, HyscaleSpecFields.external);
            WorkflowLogger.persist(ValidatorActivity.INVALID_VALUE, LoggerTags.ERROR, HyscaleSpecFields.external);
            return false;
        }
        if (external) {
            return true;
        }

        //Validate Network Traffic Rules as External is False
        List<Port> servicePorts = serviceSpec.get(HyscaleSpecFields.ports, new TypeReference<>() {
        });
        List<Agent> agents = serviceSpec.get(HyscaleSpecFields.agents, new TypeReference<>() {
        });
        List<Integer> exposedPorts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(servicePorts)) {
            exposedPorts = servicePorts.stream().map(s -> Integer.parseInt(s.getPort().split("/")[0])).collect(Collectors.toList());
        }
        List<Integer> finalExposedPorts = exposedPorts;
        if (!CollectionUtils.isEmpty(agents)) {
            agents.forEach(agent -> // add agents.getPorts null check
                    {
                        if (agent.getPorts() != null) {
                            agent.getPorts().forEach(port ->
                                    finalExposedPorts.add(Integer.parseInt(port.getPort().split("/")[0]))
                            );
                        }
                    }
            );
        }
        return validateTrafficRules(networkTrafficRules, finalExposedPorts);
    }

    //Check for Valid Network Traffic Rules
    private boolean validateTrafficRules(List<NetworkTrafficRule> networkTrafficRules, List<Integer> exposedPorts) {
        for (NetworkTrafficRule networkTrafficRule : networkTrafficRules) {
            if (networkTrafficRule.getPorts() == null) {
                logger.info("Network traffic Rules are not Valid");
                addErrorMessage(ValidatorActivity.INVALID_NETWORK_TRAFFIC_RULES, (String) null);
                WorkflowLogger.persist(ValidatorActivity.INVALID_NETWORK_TRAFFIC_RULES, LoggerTags.ERROR);
                return false;
            }
            // Rules is invalid if port is not exposed
            for (Integer port : networkTrafficRule.getPorts()) {
                if (!exposedPorts.contains(port)) {
                    logger.info("Cannot apply traffic rules to ports that are not exposed");
                    addErrorMessage(ValidatorActivity.PORT_NOT_EXPOSED, port.toString());
                    WorkflowLogger.persist(ValidatorActivity.PORT_NOT_EXPOSED, LoggerTags.ERROR, port.toString());
                    return false;
                }
            }
        }
        return true;
    }

    private void addErrorMessage(ValidatorActivity validatorActivity, String... args) {
        if (WorkflowLogger.isDisabled()) {
            structuredOutputHandler.addErrorMessage(validatorActivity.getActivityMessage(), args);
        }
    }
}
