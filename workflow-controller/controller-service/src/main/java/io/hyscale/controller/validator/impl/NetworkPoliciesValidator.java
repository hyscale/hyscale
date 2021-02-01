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
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NetworkPoliciesValidator implements Validator<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(NetworkPoliciesValidator.class);

    @Override
    public boolean validate(WorkflowContext workflowContext) throws HyscaleException {
        logger.info("Validating Network Policies");
        ServiceSpec serviceSpec = workflowContext.getServiceSpec();
        Boolean external = serviceSpec.get(HyscaleSpecFields.external, Boolean.class);
        List<NetworkTrafficRule> networkTrafficRules = serviceSpec.get(HyscaleSpecFields.allowTraffic, new TypeReference<>() {
        });

        if (external && networkTrafficRules != null) {
            logger.info("External Cannot be True to Apply Network Traffic Rules");
            WorkflowLogger.persist(ValidatorActivity.INVALID_VALUE, LoggerTags.ERROR, HyscaleSpecFields.external);
            return false;
        }

        if (!external) {
            if (CollectionUtils.isEmpty(networkTrafficRules)) {
                return true;
            }

            for (NetworkTrafficRule networkTrafficRule : networkTrafficRules) {
                if (networkTrafficRule.getPorts() == null || networkTrafficRule.getFrom() == null) {
                    logger.info("Network traffic Rules are not Valid");
                    WorkflowLogger.persist(ValidatorActivity.INVALID_NETWORK_TRAFFIC_RULES, LoggerTags.ERROR);
                    return false;
                }
                List<Port> portList = serviceSpec.get(HyscaleSpecFields.ports, new TypeReference<List<Port>>() {});
                List<Agent> agents = serviceSpec.get(HyscaleSpecFields.agents, new TypeReference<List<Agent>>() {});
                if(!CollectionUtils.isEmpty(agents)) {
                    agents.stream().forEach(agent -> {
                        portList.addAll(agent.getPorts());
                    });
                }
                List<Integer> ports = portList.stream().map(s -> Integer.parseInt(s.getPort().split("/")[0])).collect(Collectors.toList());
                for (Integer port : networkTrafficRule.getPorts()) {
                    if (!ports.contains(port)) {
                        logger.info("Cannot apply traffic rules to ports that are not exposed");
                        WorkflowLogger.persist(ValidatorActivity.PORT_NOT_EXPOSED, LoggerTags.ERROR, port.toString());
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
