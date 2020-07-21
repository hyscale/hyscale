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
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.model.PodChecksum;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.PropType;
import io.hyscale.servicespec.commons.model.service.Props;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigMapDataUtil {

    private static final Logger logger = LoggerFactory.getLogger(ConfigMapDataUtil.class);
    
    private ConfigMapDataUtil() {}
    
    private static final String DATA_PATH = "data";
    
    private static final String BINARY_DATA_PATH = "binaryData";

    public static List<ManifestSnippet> build(Props props, String propsVolumePath) throws JsonProcessingException {
        List<ManifestSnippet> manifestSnippetList = new ArrayList<ManifestSnippet>();
        if (props == null) {
            return manifestSnippetList;
        }
        Map<String, String> configProps = new HashMap<>();
        Map<String, String> fileProps = new HashMap<>();
        StringBuilder sb = new StringBuilder();

        props.getProps().entrySet().stream().forEach(each -> {
            String value = each.getValue();
            if (PropType.FILE.getPatternMatcher().matcher(value).matches()) {
                String fileContent = null;
                try (InputStream is = new FileInputStream(SetupConfig.getAbsolutePath(PropType.FILE.extractPropValue(value)))) {
                    fileContent = IOUtils.toString(is, ToolConstants.CHARACTER_ENCODING);
                    logger.debug(" Adding file {} to file props.", value);
                    fileProps.put(each.getKey(), Base64.encodeBase64String(fileContent.getBytes()));
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
        /**
         *  Consolidating all the props (string & endpoint) when propsVolumePath is defined as @see ManifestGenConstants.DEFAULT_CONFIG_PROPS_FILE
         */
        if (StringUtils.isNotBlank(fileData) && StringUtils.isNotBlank(propsVolumePath)) {
            logger.debug("Consolidating config props to file {}", ManifestGenConstants.DEFAULT_CONFIG_PROPS_FILE);
            configProps.put(ManifestGenConstants.DEFAULT_CONFIG_PROPS_FILE, fileData);
        }

        ManifestSnippet configMapDataSnippet = new ManifestSnippet();
        configMapDataSnippet.setKind(ManifestResource.CONFIG_MAP.getKind());
        configMapDataSnippet.setPath(DATA_PATH);
        configMapDataSnippet.setSnippet(JsonSnippetConvertor.serialize(configProps));
        manifestSnippetList.add(configMapDataSnippet);

        ManifestSnippet binaryDataSnippet = new ManifestSnippet();
        binaryDataSnippet.setKind(ManifestResource.CONFIG_MAP.getKind());
        binaryDataSnippet.setPath(BINARY_DATA_PATH);
        binaryDataSnippet.setSnippet(JsonSnippetConvertor.serialize(fileProps));
        manifestSnippetList.add(binaryDataSnippet);

        return manifestSnippetList;
    }
    
    public static PodChecksum.Prop getPodChecksumProp(List<ManifestSnippet> configMapSnippets){
        if (configMapSnippets == null || configMapSnippets.isEmpty()) {
            return null;
        }
        PodChecksum.Prop propChecksum = new PodChecksum().new Prop();
        configMapSnippets.stream().forEach(snippet -> {
            if (DATA_PATH.equals(snippet.getPath())) {
                propChecksum.setData(snippet.getSnippet());
            }
            if (BINARY_DATA_PATH.equals(snippet.getPath())) {
                propChecksum.setBinaryData(snippet.getSnippet());
            }
        });
        return propChecksum;
    }
    
    public static void updatePodChecksum(List<ManifestSnippet> configMapSnippets, ManifestContext manifestContext,
            String agentName) {
        if (configMapSnippets == null || configMapSnippets.isEmpty()) {
            return;
        }
        PodChecksum.Prop prop = getPodChecksumProp(configMapSnippets);
        if (prop == null) {
            return;
        }

        Object podChecksumObj = manifestContext.getGenerationAttribute(ManifestGenConstants.POD_CHECKSUM);
        PodChecksum podChecksum = podChecksumObj == null ? new PodChecksum() : (PodChecksum) podChecksumObj;

        if (StringUtils.isBlank(agentName)) {
            podChecksum.setProp(prop);
        } else {
            podChecksum.addAgentProp(agentName, prop);
        }
        manifestContext.addGenerationAttribute(ManifestGenConstants.POD_CHECKSUM, podChecksum);
    }
}
