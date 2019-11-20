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
import io.hyscale.generator.services.generator.MetadataManifestSnippetGenerator;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.Props;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.models.V1ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgentConfigMapBuilder implements AgentBuilder {
    private static final Logger logger = LoggerFactory.getLogger(AgentConfigMapBuilder.class);

    @Override
    public List<ManifestSnippet> build(List<Agent> agents, ServiceSpec serviceSpec) throws JsonProcessingException {
        List<ManifestSnippet> configMapSnippets = new ArrayList<ManifestSnippet>();
        for (Agent agent : agents) {
            if(agent.getProps() == null || agent.getProps().isEmpty()){
                continue;
            }
            String configMapName = generateConfigMapName(agent.getName());
            configMapSnippets.addAll(createConfigMapSnippet(configMapName));
            Props props = new Props();
            props.setProps(agent.getProps());
            String propsVolumePath = agent.getPropsVolumePath();
            List<ManifestSnippet> agentConfigMapSnippets = ConfigMapDataUtil.build(props,propsVolumePath);
            agentConfigMapSnippets.forEach(manifestSnippet -> {
                manifestSnippet.setName(configMapName);
            });
            configMapSnippets.addAll(agentConfigMapSnippets);
        }
        return configMapSnippets;
    }

    private String generateConfigMapName(String agentName) {
        return "agent-" + agentName;
    }

    private List<ManifestSnippet> createConfigMapSnippet(String configMapName) throws JsonProcessingException {
        List<ManifestSnippet> configMapSnippets = new ArrayList<ManifestSnippet>();
        ManifestSnippet kindSnippet = MetadataManifestSnippetGenerator.getKind(ManifestResource.CONFIG_MAP);
        kindSnippet.setName(configMapName);
        configMapSnippets.add(kindSnippet);

        ManifestSnippet apiVersionSnippet = MetadataManifestSnippetGenerator.getApiVersion(ManifestResource.CONFIG_MAP, null);
        apiVersionSnippet.setName(configMapName);
        configMapSnippets.add(apiVersionSnippet);

        ManifestSnippet snippet = new ManifestSnippet();
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(configMapName);
        // TODO Set labels
        snippet.setSnippet(JsonSnippetConvertor.serialize(v1ObjectMeta));
        snippet.setPath("metadata");
        snippet.setKind(ManifestResource.CONFIG_MAP.getKind());
        snippet.setName(configMapName);
        configMapSnippets.add(snippet);
        return configMapSnippets;
    }
}
