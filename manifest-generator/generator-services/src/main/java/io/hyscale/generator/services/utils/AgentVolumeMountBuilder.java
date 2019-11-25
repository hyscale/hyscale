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
import io.hyscale.generator.services.generator.K8sResourceNameGenerator;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.AgentVolume;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.models.V1VolumeMount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Pod spec manifest snippet builder for volume mounts in agents.
 * <p>
 * This class is responsible for building manifest snippets related to volume mounts
 * in container spec for agents.
 * </p>
 *
 */
@Component
public class AgentVolumeMountBuilder implements AgentBuilder {
    @Autowired
    AgentManifestNameGenerator agentManifestNameGenerator;
    @Override
    public List<ManifestSnippet> build(List<Agent> agents, ServiceSpec serviceSpec) throws JsonProcessingException {
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();
        List<ManifestSnippet> volumeMountSnippets = new ArrayList<ManifestSnippet>();
        int agentCount = 1;
        for (Agent agent : agents) {
            String propsVolumePath = agent.getPropsVolumePath();
            String secretsVolumePath = agent.getSecretsVolumePath();
            ManifestSnippet volumeMountSnippet = new ManifestSnippet();
            volumeMountSnippet.setKind(podSpecOwner);
            volumeMountSnippet.setPath("spec.template.spec.containers[" + agentCount + "].volumeMounts");
            List<AgentVolume> volumes = agent.getVolumes();
            agentCount++;
            List<V1VolumeMount> volumeMounts = new ArrayList<V1VolumeMount>();
            if (volumes != null && !volumes.isEmpty()) {
                volumes.stream().forEach(volume -> {
                    V1VolumeMount volumeMount = new V1VolumeMount();
                    volumeMount.setName(volume.getName());
                    volumeMount.setMountPath(volume.getPath());
                    volumeMount.setReadOnly(volume.isReadOnly());
                    volumeMounts.add(volumeMount);
                });
            }
            if (agent.getProps() != null && !agent.getProps().isEmpty()) {
                String configMapName = agentManifestNameGenerator.generateConfigMapName(agent.getName());
                volumeMounts.add(VolumeMountsUtil.buildForProps(propsVolumePath,
                        K8sResourceNameGenerator.getResourceVolumeName(configMapName, ManifestResource.CONFIG_MAP.getKind())));
            }
            if (agent.getSecrets() != null) {
                String secretName = agentManifestNameGenerator.generateSecretName(agent.getName());
                volumeMounts.add(VolumeMountsUtil.buildForSecrets(secretsVolumePath,
                        K8sResourceNameGenerator.getResourceVolumeName(secretName, ManifestResource.SECRET.getKind())));
            }
            volumeMountSnippet.setSnippet(JsonSnippetConvertor.serialize(volumeMounts));
            volumeMountSnippets.add(volumeMountSnippet);
        }

        return volumeMountSnippets;
    }
}
