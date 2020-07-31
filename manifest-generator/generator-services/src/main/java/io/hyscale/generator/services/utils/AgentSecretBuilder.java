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
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.builder.DefaultLabelBuilder;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.generator.MetadataManifestSnippetGenerator;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.Secrets;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Secrets manifest snippet builder for agents.
 * <p>
 * This class is responsible for building manifest snippets related to secrets
 * for agents.
 * </p>
 *
 */
@Component
public class AgentSecretBuilder extends AgentHelper implements AgentBuilder {

    @Autowired
    AgentManifestNameGenerator agentManifestNameGenerator;
    private static final Logger logger = LoggerFactory.getLogger(AgentSecretBuilder.class);
    @Override
    public List<ManifestSnippet> build(ManifestContext manifestContext, ServiceSpec serviceSpec) throws JsonProcessingException, HyscaleException {
        List<ManifestSnippet> secretSnippets = new ArrayList<>();
        List<Agent> agents = getAgents(serviceSpec);
        String serviceName = serviceSpec.get(HyscaleSpecFields.name,String.class);
        if(agents == null){
            return secretSnippets;
        }
        for (Agent agent : agents) {
            Secrets secrets = agent.getSecrets();
            if (secrets == null) {
                continue;
            }
            String secretName = agentManifestNameGenerator.generateSecretName(agent.getName(),serviceName);
            secretSnippets.addAll(createSecretSnippet(secretName,manifestContext,serviceSpec));
            String secretsVolumePath = agent.getSecretsVolumePath();
            ManifestSnippet secretSnippet = SecretsDataUtil.build(secrets, secretsVolumePath, ManifestGenConstants.DEFAULT_SECRETS_FILE);
            SecretsDataUtil.updatePodChecksum(secretSnippet, manifestContext, agent.getName());
            if (secretSnippet != null) {
                secretSnippet.setName(secretName);
                secretSnippets.add(secretSnippet);
            }
        }
        return secretSnippets;
    }

    private List<ManifestSnippet> createSecretSnippet(String secretName, ManifestContext manifestContext, ServiceSpec serviceSpec) throws JsonProcessingException {
        String appName = manifestContext.getAppName();
        String envName = manifestContext.getEnvName();
        String serviceName = null;
        try{
            serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
        }catch (HyscaleException e){
            logger.error("Error fetching service name from service spec",e);
        }
        List<ManifestSnippet> secretSnippets = new ArrayList<>();
        ManifestSnippet kindSnippet = MetadataManifestSnippetGenerator.getKind(ManifestResource.SECRET);
        kindSnippet.setName(secretName);
        secretSnippets.add(kindSnippet);

        ManifestSnippet apiVersionSnippet = MetadataManifestSnippetGenerator.getApiVersion(ManifestResource.SECRET);
        apiVersionSnippet.setName(secretName);
        secretSnippets.add(apiVersionSnippet);

        ManifestSnippet snippet = new ManifestSnippet();
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setName(secretName);
        v1ObjectMeta.setLabels(DefaultLabelBuilder.build(appName,envName,serviceName));
        snippet.setSnippet(JsonSnippetConvertor.serialize(v1ObjectMeta));
        snippet.setPath("metadata");
        snippet.setKind(ManifestResource.SECRET.getKind());
        snippet.setName(secretName);
        secretSnippets.add(snippet);
        return secretSnippets;
    }
}
