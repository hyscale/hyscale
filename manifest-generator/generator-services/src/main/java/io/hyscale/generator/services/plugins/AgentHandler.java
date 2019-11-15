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
import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.DecoratedArrayList;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.generator.MetadataManifestSnippetGenerator;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.PropType;
import io.hyscale.servicespec.commons.model.service.*;
import io.kubernetes.client.models.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author nishanth.panthangi created on 05-Nov-2019
 */
@Component
@ManifestPlugin(name = "AgentHandler")
public class AgentHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentHandler.class);
    private static final String DEFAULT_IMAGE_PULL_POLICY = "Always";
    //  private static final String DEFAULT_IMAGE_PULL_POLICY = "IfNotPresent";
    //  config map name = "agent-{agent.name}-configmap"

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        if (!ManifestPredicates.getAgentsPredicate().test(serviceSpec)) {
            logger.debug("Agents found to be empty while processing service spec data.");
            return null;
        }
        TypeReference<List<Agent>> agentsList = new TypeReference<List<Agent>>() {
        };
        List<Agent> agents = null;
        try {
            agents = serviceSpec.get(HyscaleSpecFields.agents, agentsList);
        } catch (HyscaleException e) {
            logger.error("Couldn't fetch agents, returning null",e);
            return null;
        }
        List<ManifestSnippet> manifestSnippetList = new ArrayList<ManifestSnippet>();

        try {
            manifestSnippetList.addAll(generateConfigMapAndSecretMetadata(agents));
            manifestSnippetList.addAll(getAgentConfigMapDataSnippet(agents));
            manifestSnippetList.addAll(getAgentSecretDataSnippet(agents));
            manifestSnippetList.addAll(getAgentPodSnippet(serviceSpec, agents));
            manifestSnippetList.addAll(getAgentEnv(serviceSpec, agents));
            manifestSnippetList.addAll(getAgentVolumeMounts(serviceSpec, agents));
            manifestSnippetList.add(getAgentVolumes(serviceSpec, agents));
        } catch (JsonProcessingException e) {
            logger.error("Json Processing Exception", e);
        }
        return manifestSnippetList;
    }

    private List<ManifestSnippet> generateConfigMapAndSecretMetadata(List<Agent> agents) throws JsonProcessingException {
        List<ManifestSnippet> manifestSnippetList = new ArrayList<ManifestSnippet>();
        for (Agent agent : agents) {
            if (agent.getProps() != null && !agent.getProps().getProps().isEmpty()) {
                String configMapName = generateConfigMapName(agent.getName());
                manifestSnippetList.addAll(createConfigMapSnippet(configMapName));

            }
            if (agent.getSecrets() != null && !agent.getSecrets().getSecretsMap().isEmpty()) {
                String secretName = generateSecretName(agent.getName());
                manifestSnippetList.addAll(createSecretSnippet(secretName));
            }
        }
        return manifestSnippetList;
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
        //    v1ObjectMeta.setLabels();
        snippet.setSnippet(JsonSnippetConvertor.serialize(v1ObjectMeta));
        snippet.setPath("metadata");
        snippet.setKind(ManifestResource.SECRET.getKind());
        snippet.setName(secretName);
        secretSnippets.add(snippet);
        return secretSnippets;
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
        //    v1ObjectMeta.setLabels();
        snippet.setSnippet(JsonSnippetConvertor.serialize(v1ObjectMeta));
        snippet.setPath("metadata");
        snippet.setKind(ManifestResource.CONFIG_MAP.getKind());
        snippet.setName(configMapName);
        configMapSnippets.add(snippet);
        return configMapSnippets;
    }

    private ManifestSnippet getAgentVolumes(ServiceSpec serviceSpec, List<Agent> agents) throws JsonProcessingException {
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();
        ManifestSnippet volumeSnippet = new ManifestSnippet();
        volumeSnippet.setKind(podSpecOwner);
        volumeSnippet.setPath("spec.template.spec.volumes");
        List<V1Volume> volumeList = new ArrayList<V1Volume>();
        for (Agent agent : agents) {
            if (agent.getProps() != null && !agent.getProps().getProps().isEmpty()) {
                V1Volume volume = new V1Volume();
                String configMapName = generateConfigMapName(agent.getName());
                volume.setName(configMapName);
                V1ConfigMapVolumeSource v1ConfigMapVolumeSource = new V1ConfigMapVolumeSource();
                v1ConfigMapVolumeSource.setName(configMapName);
                volume.setConfigMap(v1ConfigMapVolumeSource);
                volumeList.add(volume);
            }
            if (agent.getSecrets() != null && !agent.getSecrets().getSecretsMap().isEmpty()) {
                V1Volume volume = new V1Volume();
                String secretName = generateSecretName(agent.getName());
                volume.setName(secretName);
                V1SecretVolumeSource v1SecretVolumeSource = new V1SecretVolumeSource();
                v1SecretVolumeSource.secretName(secretName);
                volume.setSecret(v1SecretVolumeSource);
                volumeList.add(volume);
            }
        }
        volumeSnippet.setSnippet(JsonSnippetConvertor.serialize(volumeList));
        return volumeSnippet;
    }

    private List<ManifestSnippet> getAgentVolumeMounts(ServiceSpec serviceSpec, List<Agent> agents) throws JsonProcessingException {
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
            if (agent.getProps() != null && !agent.getProps().getProps().isEmpty()) {
                V1VolumeMount volumeMount = new V1VolumeMount();
                String configMapName = generateConfigMapName(agent.getName());
                volumeMount.setName(configMapName);
                volumeMount.setMountPath(propsVolumePath);
                volumeMount.setReadOnly(true);
                volumeMounts.add(volumeMount);
            }
            if (agent.getSecrets() != null && !agent.getSecrets().getSecretsMap().isEmpty()) {
                V1VolumeMount volumeMount = new V1VolumeMount();
                String secretName = generateSecretName(agent.getName());
                volumeMount.setName(secretName);
                volumeMount.setMountPath(secretsVolumePath);
                volumeMount.setReadOnly(true);
                volumeMounts.add(volumeMount);
            }
            volumeMountSnippet.setSnippet(JsonSnippetConvertor.serialize(volumeMounts));
            volumeMountSnippets.add(volumeMountSnippet);
        }

        return volumeMountSnippets;
    }

    private List<ManifestSnippet> getAgentEnv(ServiceSpec serviceSpec, List<Agent> agents) throws JsonProcessingException {
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
            if (agent.getProps() != null) {
                envVarList.addAll(getPropEnv(agent.getProps(), generateConfigMapName(agent.getName())));
            }
            if (agent.getSecrets() != null) {
                envVarList.addAll(getSecretEnv(agent.getSecrets(), generateSecretName(agent.getName())));
            }
            if (!envVarList.isEmpty()) {
                agentEnvSnippet.setSnippet(JsonSnippetConvertor.serialize(envVarList));
                envSnippets.add(agentEnvSnippet);
            }
        }
        logger.debug("4. Agent Env Snippets: " + JsonSnippetConvertor.serialize(envSnippets));
        return envSnippets;
    }

    private List<V1EnvVar> getSecretEnv(Secrets secrets, String secretName) {
        if (secrets == null || secrets.getSecretsMap().isEmpty()) {
            return null;
        }
        List<V1EnvVar> envVarList = new DecoratedArrayList<V1EnvVar>();
        secrets.getSecretKeys().stream().forEach(each -> {
            V1EnvVar envVar = new V1EnvVar();
            envVar.setName(each);

            V1EnvVarSource envVarSource = new V1EnvVarSource();
            V1SecretKeySelector secretKeySelector = new V1SecretKeySelector();
            secretKeySelector.setName(secretName);
            secretKeySelector.setKey(each);
            envVarSource.setSecretKeyRef(secretKeySelector);
            envVar.setValueFrom(envVarSource);
            envVarList.add(envVar);
        });
        return envVarList;
    }

    private List<V1EnvVar> getPropEnv(Props props, String configMapName) {
        if (props == null || props.getProps().isEmpty()) {
            return null;
        }
        List<V1EnvVar> envVarList = new DecoratedArrayList<V1EnvVar>();
        props.getProps().entrySet().stream().forEach(each -> {
            V1EnvVar envVar = new V1EnvVar();
            envVar.setName(each.getKey());

            V1EnvVarSource envVarSource = new V1EnvVarSource();
            V1ConfigMapKeySelector configMapKeySelector = new V1ConfigMapKeySelector();
            configMapKeySelector.setName(configMapName);
            configMapKeySelector.setKey(each.getKey());
            envVarSource.setConfigMapKeyRef(configMapKeySelector);
            envVar.setValueFrom(envVarSource);
            envVarList.add(envVar);
        });
        return envVarList;
    }

    private List<ManifestSnippet> getAgentPodSnippet(ServiceSpec serviceSpec, List<Agent> agents) throws JsonProcessingException {
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ? ManifestResource.STATEFUL_SET.getKind() :
                ManifestResource.DEPLOYMENT.getKind();
        List<ManifestSnippet> podSnippets = new ArrayList<ManifestSnippet>();
        int agentCount = 1;
        for (Agent agent : agents) {
            // name
            ManifestSnippet agentNameSnippet = new ManifestSnippet();
            agentNameSnippet.setKind(podSpecOwner);
            agentNameSnippet.setSnippet(agent.getName());
            String namePath = "spec.template.spec.containers[" + agentCount + "].name";
            agentNameSnippet.setPath(namePath);
            podSnippets.add(agentNameSnippet);
            // image
            ManifestSnippet agentImageSnippet = new ManifestSnippet();
            agentImageSnippet.setKind(podSpecOwner);
            agentImageSnippet.setSnippet(agent.getImage());
            String imagePath = "spec.template.spec.containers[" + agentCount + "].image";
            agentImageSnippet.setPath(imagePath);
            podSnippets.add(agentImageSnippet);
            // pull policy
            ManifestSnippet agentPullPolicySnippet = new ManifestSnippet();
            agentPullPolicySnippet.setKind(podSpecOwner);
            agentPullPolicySnippet.setSnippet(DEFAULT_IMAGE_PULL_POLICY);
            String pullPolicyPath = "spec.template.spec.containers[" + agentCount + "].imagePullPolicy";
            agentPullPolicySnippet.setPath(pullPolicyPath);
            podSnippets.add(agentPullPolicySnippet);

            agentCount++;
        }
        return podSnippets;
    }

    private List<ManifestSnippet> getAgentSecretDataSnippet(List<Agent> agents) throws JsonProcessingException {
        List<ManifestSnippet> secretSnippets = new ArrayList<ManifestSnippet>();
        for (Agent agent : agents) {
            ManifestSnippet secretSnippet = new ManifestSnippet();
            Secrets secrets = agent.getSecrets();
            Map<String, String> modifiedMap = secrets.getSecretsMap().entrySet().stream().collect(
                    Collectors.toMap(key -> key.getKey(), value -> Base64.encodeBase64String(value.getValue().getBytes())));
            StringBuilder stringBuilder = new StringBuilder();
            secrets.getSecretsMap().entrySet().stream().forEach(each -> {
                stringBuilder.append(each.getKey()).append("=").append(each.getValue()).append("\n");
            });
            modifiedMap.put("secrets.prop",
                    Base64.encodeBase64String(stringBuilder.toString().getBytes()));

            secretSnippet.setName(generateSecretName(agent.getName()));
            secretSnippet.setKind(ManifestResource.SECRET.getKind());
            secretSnippet.setPath("data");
            secretSnippet.setSnippet(JsonSnippetConvertor.serialize(modifiedMap));
            secretSnippets.add(secretSnippet);
        }
        return secretSnippets;
    }

    private List<ManifestSnippet> getAgentConfigMapDataSnippet(List<Agent> agents) throws JsonProcessingException {
        List<ManifestSnippet> configMapSnippets = new ArrayList<ManifestSnippet>();
        for (Agent agent : agents) {
            ManifestSnippet agentConfigMapSnippet = new ManifestSnippet();
            Props props = agent.getProps();
            Map<String, String> configProps = new HashMap<>();
            StringBuilder sb = new StringBuilder();
            props.getProps().entrySet().stream().forEach(each -> {
                String value = each.getValue();
                if (PropType.FILE.getPatternMatcher().matcher(value).matches()) {
                    String fileContent = null;
                    try (InputStream is = new FileInputStream(SetupConfig.getAbsolutePath(PropType.FILE.extractPropValue(value)))) {
                        fileContent = IOUtils.toString(is, ToolConstants.CHARACTER_ENCODING);
                        logger.debug(" Adding file {} to config props.", value);
                        configProps.put(each.getKey(), Base64.encodeBase64String(fileContent.getBytes()));
                    } catch (IOException e) {
                        logger.error("Error while reading file content of config prop {}", each.getKey(), e);
                    }
                } else if (PropType.ENDPOINT.getPatternMatcher().matcher(value).matches()) {
                    String propValue = PropType.ENDPOINT.extractPropValue(each.getValue());
                    configProps.put(each.getKey(), propValue);
                    logger.debug(" Adding endpoint {} to config props.", value);
                    sb.append(each.getKey()).append("=").append(propValue).append("\n");
                } else {
                    String propValue = PropType.STRING.extractPropValue(each.getValue());
                    configProps.put(each.getKey(), propValue);
                    logger.debug(" Adding prop {} to config props.", value);
                    sb.append(each.getKey()).append("=").append(propValue).append("\n");
                }
            });

            String fileData = sb.toString();
            if (StringUtils.isNotBlank(fileData)) {
                configProps.put("config.props", fileData);
            }
            agentConfigMapSnippet.setSnippet(JsonSnippetConvertor.serialize(configProps));
            agentConfigMapSnippet.setKind(ManifestResource.CONFIG_MAP.getKind());
            agentConfigMapSnippet.setPath("data");
            agentConfigMapSnippet.setName(generateConfigMapName(agent.getName()));
            configMapSnippets.add(agentConfigMapSnippet);
        }
        return configMapSnippets;
    }

    private String generateConfigMapName(String agentName) {
        return "agent-" + agentName + "-configmap";
    }

    private String generateSecretName(String agentName) {
        return "agent-" + agentName + "-secret";
    }
}
