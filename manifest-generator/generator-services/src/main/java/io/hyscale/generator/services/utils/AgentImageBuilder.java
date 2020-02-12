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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pod spec manifest snippet builder for agent image information.
 * <p>
 * This class is responsible for building manifest snippets
 * for agent name,image and image pull policy.
 * </p>
 *
 */
@Component
public class AgentImageBuilder extends AgentHelper implements AgentBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AgentImageBuilder.class);

    @Override
    public List<ManifestSnippet> build(ManifestContext manifestContext, ServiceSpec serviceSpec) throws JsonProcessingException {
        String podSpecOwner = ((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
        List<ManifestSnippet> podSnippets = new ArrayList<ManifestSnippet>();
        List<Agent> agents = getAgents(serviceSpec);
        if(agents == null){
            return podSnippets;
        }
        int agentCount = 1;
        for (Agent agent : agents) {
            String pathPrefix = "spec.template.spec.containers[" + agentCount+"].";
            // name
            ManifestSnippet agentNameSnippet = new ManifestSnippet();
            agentNameSnippet.setKind(podSpecOwner);
            agentNameSnippet.setSnippet(agent.getName());
            String namePath = pathPrefix+"name";
            agentNameSnippet.setPath(namePath);
            podSnippets.add(agentNameSnippet);
            // image
            ManifestSnippet agentImageSnippet = new ManifestSnippet();
            agentImageSnippet.setKind(podSpecOwner);
            agentImageSnippet.setSnippet(agent.getImage());
            String imagePath = pathPrefix+"image";
            agentImageSnippet.setPath(imagePath);
            podSnippets.add(agentImageSnippet);
            // pull policy
            ManifestSnippet agentPullPolicySnippet = new ManifestSnippet();
            agentPullPolicySnippet.setKind(podSpecOwner);
            agentPullPolicySnippet.setSnippet(ManifestGenConstants.DEFAULT_IMAGE_PULL_POLICY);
            String pullPolicyPath = pathPrefix+"imagePullPolicy";
            agentPullPolicySnippet.setPath(pullPolicyPath);
            podSnippets.add(agentPullPolicySnippet);

            agentCount++;
        }
        return podSnippets;
    }
}
