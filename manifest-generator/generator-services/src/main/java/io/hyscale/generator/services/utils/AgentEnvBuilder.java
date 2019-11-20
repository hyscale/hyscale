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
import io.hyscale.commons.models.DecoratedArrayList;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.Props;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.models.V1EnvVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgentEnvBuilder implements AgentBuilder {
    private static final Logger logger = LoggerFactory.getLogger(AgentEnvBuilder.class);

    @Override
    public List<ManifestSnippet> build(List<Agent> agents, ServiceSpec serviceSpec) throws JsonProcessingException {
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ? ManifestResource.STATEFUL_SET.getKind() :
                ManifestResource.DEPLOYMENT.getKind();
        List<ManifestSnippet> envSnippets = new ArrayList<ManifestSnippet>();
        int agentCount = 1;
        for (Agent agent : agents) {
            ManifestSnippet agentEnvSnippet = new ManifestSnippet();
            agentEnvSnippet.setKind(podSpecOwner);
            agentEnvSnippet.setPath("spec.template.spec.containers[" + agentCount + "].env");
            agentCount++;
            List<V1EnvVar> envVarList = new DecoratedArrayList<V1EnvVar>();
            if (agent.getProps() != null && !agent.getProps().isEmpty()) {
                String configMapName = generateConfigMapName(agent.getName());
                Props props = new Props();
                props.setProps(agent.getProps());
                envVarList.addAll(PodSpecEnvUtil.getPropEnv(props, configMapName));
            }
            if (agent.getSecrets() != null) {
                String secretName = generateSecretName(agent.getName());
                envVarList.addAll(PodSpecEnvUtil.getSecretEnv(agent.getSecrets(), secretName));
            }
            if (!envVarList.isEmpty()) {
                agentEnvSnippet.setSnippet(JsonSnippetConvertor.serialize(envVarList));
                envSnippets.add(agentEnvSnippet);
            }
        }
        return envSnippets;
    }

    private String generateConfigMapName(String agentName) {
        return "agent-" + agentName;
    }

    private String generateSecretName(String agentName) {
        return "agent-" + agentName;
    }
}
