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
package io.hyscale.generator.services.utils;

import com.google.common.collect.Lists;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.builder.DefaultPortsBuilder;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgentPortBuilder extends AgentHelper implements AgentBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AgentPortBuilder.class);

    @Override
    public List<ManifestSnippet> build(ManifestContext manifestContext, ServiceSpec serviceSpec) throws HyscaleException {
        List<ManifestSnippet> manifestSnippetList = Lists.newArrayList();
        List<Agent> agents = getAgents(serviceSpec);
        if (BooleanUtils.isTrue(agents.isEmpty())) {
            return manifestSnippetList;
        }
        List<Port> portList;
        for (Agent agent : agents) {
            portList = agent.getPorts();
            String podSpecOwner = ((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
            logger.info("Processing Ports for Agent {} ", agent.getName());
            DefaultPortsBuilder.generatePortsManifest(portList, podSpecOwner, manifestSnippetList);
        }
        return manifestSnippetList;
    }
}
