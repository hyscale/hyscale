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
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PortsValidator implements Validator<WorkflowContext> {
    private static final Logger logger = LoggerFactory.getLogger(PortsValidator.class);

    @Override
    public boolean validate(WorkflowContext workflowContext) throws HyscaleException {

        ServiceSpec serviceSpec = workflowContext.getServiceSpec();
        List<Port> servicePorts = serviceSpec.get(HyscaleSpecFields.ports, new TypeReference<>() {
        });
        List<Agent> agents = serviceSpec.get(HyscaleSpecFields.agents, new TypeReference<>() {
        });
        if(!CollectionUtils.isEmpty(servicePorts)) {
            List<Integer> exposedPorts = servicePorts.stream().map(s -> Integer.parseInt(s.getPort().split("/")[0])).collect(Collectors.toList());
            // Check for duplicate ports being exposed in service or agents
            if (!CollectionUtils.isEmpty(agents)) {
                for (Agent agent : agents) {
                    for (Port port : agent.getPorts()) {
                        if (exposedPorts.contains(Integer.parseInt(port.getPort().split("/")[0]))) {
                            logger.info("Duplicate Port Exposed in Spec {} ", port.getPort());
                            WorkflowLogger.persist(ValidatorActivity.DUPLICATE_PORTS, LoggerTags.ERROR, port.getPort());
                            return false;
                        }
                        exposedPorts.add(Integer.parseInt(port.getPort().split("/")[0]));
                    }
                }
            }
        }
        return true;
    }
}
