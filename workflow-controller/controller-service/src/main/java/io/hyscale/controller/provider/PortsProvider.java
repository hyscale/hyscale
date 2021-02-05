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
package io.hyscale.controller.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PortsProvider {

    public List<Integer> getExposedPorts(ServiceSpec serviceSpec, boolean agentPorts) throws HyscaleException {
        List<Port> servicePorts = serviceSpec.get(HyscaleSpecFields.ports, new TypeReference<>() {
        });
        List<Integer> exposedPorts = CollectionUtils.isEmpty(servicePorts) ? new ArrayList<>() :
                servicePorts.stream().map(s -> Integer.parseInt(s.getPort().split("/")[0])).collect(Collectors.toList());
        if (agentPorts) {
            List<Agent> agents = serviceSpec.get(HyscaleSpecFields.agents, new TypeReference<>() {
            });
            if (!CollectionUtils.isEmpty(agents)) {
                agents.stream().filter(each -> each.getPorts() != null).forEach(agent ->
                    agent.getPorts().forEach(port ->
                        exposedPorts.add(Integer.parseInt(port.getPort().split("/")[0]))));
            }
        }
        return exposedPorts;
    }
}
