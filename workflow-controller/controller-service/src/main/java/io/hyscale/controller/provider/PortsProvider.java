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

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.generator.services.builder.DefaultPortsBuilder;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides a list of Exposed Ports from
 * the service spec provided.
 */

@Component
public class PortsProvider {

    @Autowired
    DefaultPortsBuilder defaultPortsBuilder;

    public List<String> getExposedPorts(ServiceSpec serviceSpec, boolean includeAgentPorts) throws HyscaleException {
        List<Port> servicePorts = serviceSpec.get(HyscaleSpecFields.ports, new TypeReference<>() {
        });
        List<String> exposedPorts = CollectionUtils.isEmpty(servicePorts) ? new ArrayList<>() :
                servicePorts.stream().map(port -> defaultPortsBuilder.updatePortProtocol(port).getPort()).collect(Collectors.toList());
        if (includeAgentPorts) {
            List<Agent> agents = serviceSpec.get(HyscaleSpecFields.agents, new TypeReference<>() {
            });
            if (!CollectionUtils.isEmpty(agents)) {
                agents.stream().filter(each -> each.getPorts() != null).forEach(agent ->
                        agent.getPorts().forEach(port ->
                                exposedPorts.add(defaultPortsBuilder.updatePortProtocol(port).getPort())));
            }
        }
        return exposedPorts;
    }
}
