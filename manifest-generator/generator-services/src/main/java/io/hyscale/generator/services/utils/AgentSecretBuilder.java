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
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.generator.MetadataManifestSnippetGenerator;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.Secrets;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.models.V1ObjectMeta;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AgentSecretBuilder implements AgentBuilder {

    @Override
    public List<ManifestSnippet> build(List<Agent> agents, ServiceSpec serviceSpec) throws JsonProcessingException {
        List<ManifestSnippet> secretSnippets = new ArrayList<ManifestSnippet>();
        for (Agent agent : agents) {
            //   Secrets secrets = agent.getSecrets();
            Secrets secrets = agent.getSecrets();
            if (secrets == null) {
                continue;
            }
            String secretName = generateSecretName(agent.getName());
            secretSnippets.addAll(createSecretSnippet(secretName));
            String secretsVolumePath = agent.getSecretsVolumePath();
            ManifestSnippet secretSnippet = SecretsDataUtil.build(secrets, secretsVolumePath, ManifestGenConstants.DEFAULT_SECRETS_FILE);
            if (secretSnippet != null) {
                secretSnippet.setName(secretName);
                secretSnippets.add(secretSnippet);
            }
        }
        return secretSnippets;
    }

    private String generateSecretName(String agentName) {
        return "agent-" + agentName;
    }

    private List<ManifestSnippet> createSecretSnippet(String secretName) throws JsonProcessingException {
        List<ManifestSnippet> secretSnippets = new ArrayList<ManifestSnippet>();
        ManifestSnippet kindSnippet = MetadataManifestSnippetGenerator.getKind(ManifestResource.SECRET);
        kindSnippet.setName(secretName);
        secretSnippets.add(kindSnippet);

        ManifestSnippet apiVersionSnippet = MetadataManifestSnippetGenerator.getApiVersion(ManifestResource.SECRET, null);
        apiVersionSnippet.setName(secretName);
        secretSnippets.add(apiVersionSnippet);

        ManifestSnippet snippet = new ManifestSnippet();
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(secretName);
        // TODO   v1ObjectMeta.setLabels();
        snippet.setSnippet(JsonSnippetConvertor.serialize(v1ObjectMeta));
        snippet.setPath("metadata");
        snippet.setKind(ManifestResource.SECRET.getKind());
        snippet.setName(secretName);
        secretSnippets.add(snippet);
        return secretSnippets;
    }
}
