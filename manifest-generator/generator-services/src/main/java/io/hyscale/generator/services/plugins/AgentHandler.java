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

package io.hyscale.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.generator.services.utils.AgentBuilder;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Plugin for Agent/Sidecar
 * <p>
 * This class is responsible for generating manifests based on Agents
 * context from service spec.
 * </p>
 *
 * @author Nishanth Panthangi
 */
@Component
@ManifestPlugin(name = "AgentHandler")
public class AgentHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentHandler.class);

    @Autowired
    private List<AgentBuilder> agentBuilders;

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        if (!ManifestPredicates.getAgentsPredicate().test(serviceSpec)) {
            logger.debug("Agents found to be empty while processing service spec data.");
            return Collections.emptyList();
        }
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        try {
            for (AgentBuilder agentBuilder : agentBuilders) {
                manifestSnippetList.addAll(agentBuilder.build(manifestContext, serviceSpec));
            }
        } catch (JsonProcessingException e) {
            logger.error("Json Processing Exception", e);
        }
        return manifestSnippetList;
    }
}
