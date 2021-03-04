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
import io.hyscale.generator.services.builder.DefaultLabelBuilder;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ManifestPlugin(name = "PodSpecLabels")
public class PodSpecLabels implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(PodSpecLabels.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setAppName(manifestContext.getAppName());
        serviceMetadata.setEnvName(manifestContext.getEnvName());
        serviceMetadata.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        String podSpecOwner = ((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
        try {
            ManifestSnippet metaDataSnippet = new ManifestSnippet();
            metaDataSnippet.setPath("spec.template.metadata");
            metaDataSnippet.setKind(podSpecOwner);
            metaDataSnippet.setSnippet(JsonSnippetConvertor.serialize(getTemplateMetaData(serviceMetadata, manifestContext)));
            snippetList.add(metaDataSnippet);

        } catch (JsonProcessingException e) {
            logger.error("Error while serializing Pod spec labels snippet ", e);
        }
        return snippetList;    
        }

    private V1ObjectMeta getTemplateMetaData(ServiceMetadata serviceMetadata, ManifestContext manifestContext) {
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        Map<String, String> labels = DefaultLabelBuilder.build(serviceMetadata);
        Map<String, String> addOnLabels = manifestContext.getCustomLabels();
        if (addOnLabels != null && !addOnLabels.isEmpty()) {
            labels.putAll(addOnLabels);
        }
        v1ObjectMeta.setLabels(labels);
        //TODO Add release-version ??
        return v1ObjectMeta;
    }
}
