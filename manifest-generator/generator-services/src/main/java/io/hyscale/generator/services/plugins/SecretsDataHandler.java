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
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.utils.HyscaleFilesUtil;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.model.MetaDataContext;
import io.hyscale.generator.services.provider.SecretsProvider;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Secrets;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@ManifestPlugin(name = "ConfigMapDataHandler")
public class SecretsDataHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(SecretsDataHandler.class);

    @Autowired
    private HyscaleFilesUtil filesUtil;

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        Secrets secrets = SecretsProvider.getSecrets(serviceSpec);
        if (!ManifestResource.SECRET.getPredicate().test(serviceSpec)) {
            return null;
        }

        String secretsVolumePath = serviceSpec.get(HyscaleSpecFields.secretsVolumePath, String.class);
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setAppName(manifestContext.getAppName());
        metaDataContext.setEnvName(manifestContext.getEnvName());
        metaDataContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));

        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        try {
            manifestSnippetList.add(getSecretsData(secrets, secretsVolumePath, metaDataContext));
        } catch (JsonProcessingException e) {
            logger.error("Error while generating manifest for props of service {}", metaDataContext.getServiceName(), e);
        }
        return manifestSnippetList;

    }

    private ManifestSnippet getSecretsData(Secrets secrets, String secretsVolumePath, MetaDataContext metaDataContext)
            throws JsonProcessingException {
        ManifestSnippet snippet = new ManifestSnippet();
        Map<String, String> modifiedMap = secrets.getSecretsMap().entrySet().stream().collect(
                Collectors.toMap(key -> key.getKey(), value -> Base64.encodeBase64String(value.getValue().getBytes())));

        if (StringUtils.isNotBlank(secretsVolumePath)) {
            logger.debug("Writing secrets into file {}",secretsVolumePath);
            StringBuilder stringBuilder = new StringBuilder();
            secrets.getSecretsMap().entrySet().stream().forEach(each -> {
                stringBuilder.append(each.getKey()).append("=").append(each.getValue()).append("\n");
            });
            try {
                modifiedMap.put(filesUtil.getFileName(secretsVolumePath), 
                		Base64.encodeBase64String(stringBuilder.toString().getBytes()));
            } catch (HyscaleException e) {
                logger.error("Error while processing secrets volumes path {}", secretsVolumePath);
            }
        }
        snippet.setSnippet(JsonSnippetConvertor.serialize(modifiedMap));
        snippet.setKind(ManifestResource.SECRET.getKind());
        snippet.setPath("data");
        return snippet;
    }
}
