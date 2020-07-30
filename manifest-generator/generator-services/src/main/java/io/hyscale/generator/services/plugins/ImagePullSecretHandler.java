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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DockerConfig;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.model.ManifestGeneratorActivity;
import io.hyscale.generator.services.builder.DefaultLabelBuilder;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.generator.MetadataManifestSnippetGenerator;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.Auth;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.commons.models.ImageRegistry;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.commons.utils.NormalizationUtil;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

@Component
@ManifestPlugin(name = "ImagePullSecretHandler")
public class ImagePullSecretHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImagePullSecretHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        ImageRegistry imageRegistry = manifestContext.getImageRegistry();
		if (imageRegistry == null) {
			logger.debug("ImageRegistry is found to be null, skipping image pull secret creation ");
			String registry = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class);
			registry = registry == null ? "" : registry;
			WorkflowLogger.persist(ManifestGeneratorActivity.FAILED_TO_CREATE_IMAGE_PULL_SECRET, registry, registry);
			return Collections.emptyList();
		}

        String name = imageRegistry.getName() == null ? imageRegistry.getUrl() : imageRegistry.getName();
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setAppName(manifestContext.getAppName());
        serviceMetadata.setEnvName(manifestContext.getEnvName());
        serviceMetadata.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        logger.debug("Generated image pull secret metadata.");
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        try {
            // Override the name because image-pull-secret has either the registry-name or registry-url as the name of the manifest
            ManifestSnippet apiVersionSnippet = MetadataManifestSnippetGenerator.getApiVersion(ManifestResource.SECRET);

            //Api Version snippet
            manifestSnippetList.add(apiVersionSnippet);

            // Get the secret kind of image pull secret
            manifestSnippetList.add(MetadataManifestSnippetGenerator.getKind(ManifestResource.SECRET));
            // Get the labels of secret as image pull secret also have the same set of labels
            manifestSnippetList.add(getMetaDataSnippet(serviceMetadata, name));
            logger.debug("Added labels to image pull secret manifest snippet.");
            // Get the data of image pull secret
            manifestSnippetList.add(getDataSnippet(imageRegistry));
            logger.debug("Added the data to image pull secret manifest snippet.");
            // Get the secret type snippet as kubernetes.io/dockerconfigjson
            manifestSnippetList.add(getSecretTypeSnippet());
            //Add Name to each snippet except podSpec ImagePullSecretName
            manifestSnippetList.stream().forEach(each -> each.setName(name));
            // Adding the secret to pod
            logger.debug("Prepared image pull secret manifest for registry.");
            manifestContext.addGenerationAttribute(ManifestGenConstants.IMAGE_PULL_SECRET_NAME, NormalizationUtil.normalize(name));

        } catch (JsonProcessingException e) {
            logger.error("Error while generating image pull secret manifest", e);
        }
        return manifestSnippetList;
    }


    private ManifestSnippet getSecretTypeSnippet() {
        ManifestSnippet secretTypeSnippet = new ManifestSnippet();
        secretTypeSnippet.setKind(ManifestResource.SECRET.getKind());
        secretTypeSnippet.setPath("type");
        secretTypeSnippet.setSnippet("kubernetes.io/dockerconfigjson");
        return secretTypeSnippet;
    }

    private ManifestSnippet getMetaDataSnippet(ServiceMetadata serviceMetadata, String name)
            throws JsonProcessingException {
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setLabels(DefaultLabelBuilder.build(serviceMetadata.getAppName(), serviceMetadata.getEnvName()));
        v1ObjectMeta.setName(NormalizationUtil.normalize(name));


        ManifestSnippet snippet = new ManifestSnippet();
        snippet.setName(name);
        snippet.setSnippet(JsonSnippetConvertor.serialize(v1ObjectMeta));
        snippet.setPath("metadata");
        snippet.setKind(ManifestResource.SECRET.getKind());
        return snippet;
    }

    private ManifestSnippet getDataSnippet(ImageRegistry imageRegistry) throws JsonProcessingException {
        ManifestSnippet snippet = new ManifestSnippet();
        snippet.setName(imageRegistry.getName());
        snippet.setSnippet(JsonSnippetConvertor.serialize(getDockerConfig(imageRegistry)));
        snippet.setPath("data");
        snippet.setKind(ManifestResource.SECRET.getKind());
        return snippet;
    }

    private Map<String, String> getDockerConfig(ImageRegistry imageRegistry) throws JsonProcessingException {
        Map<String, String> dockerAuthConfigMap = new HashMap<>();
        ObjectMapper objectMapper = ObjectMapperFactory.jsonMapper();
        DockerConfig dockerAuthConfig = new DockerConfig();

        Auth auth = new Auth();
        auth.setAuth(imageRegistry.getToken());
        Map<String, Auth> auths = new HashMap<>();
        auths.put(imageRegistry.getUrl(), auth);
        dockerAuthConfig.setAuths(auths);

        String dockerConfigJson = objectMapper.writeValueAsString(dockerAuthConfig);
        String encodedValue = Base64.encodeBase64String(dockerConfigJson.getBytes());
        dockerAuthConfigMap.put(".dockerconfigjson", encodedValue);
        return dockerAuthConfigMap;
    }
}
