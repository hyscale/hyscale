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
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.Secrets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SecretsDataUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecretsDataUtil.class);

    public static ManifestSnippet build(Secrets secrets, String secretsVolumePath, String fileName) throws JsonProcessingException{
        ManifestSnippet snippet = new ManifestSnippet();
        Map<String, String> modifiedMap = secrets.getSecretsMap().entrySet().stream().collect(
                Collectors.toMap(key -> key.getKey(), value -> Base64.encodeBase64String(value.getValue().getBytes())));

        if (StringUtils.isNotBlank(secretsVolumePath)) {
            logger.debug("Writing secrets into file {}.",secretsVolumePath);
            StringBuilder stringBuilder = new StringBuilder();
            secrets.getSecretsMap().entrySet().stream().forEach(each -> {
                stringBuilder.append(each.getKey()).append("=").append(each.getValue()).append("\n");
            });
            modifiedMap.put(fileName,
                    Base64.encodeBase64String(stringBuilder.toString().getBytes()));
        }
        snippet.setSnippet(JsonSnippetConvertor.serialize(modifiedMap));
        snippet.setKind(ManifestResource.SECRET.getKind());
        snippet.setPath("data");
        return snippet;
    }
}
