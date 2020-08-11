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
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ManifestPlugin(name = "SelectorLabelsHandler")
public class SelectorLabelsHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(SelectorLabelsHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setAppName(manifestContext.getAppName());
        serviceMetadata.setEnvName(manifestContext.getEnvName());
        serviceMetadata.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        String podSpecOwner = ((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
        try {
            snippetList.add(getPodSpecSelectorLabels(serviceMetadata, podSpecOwner));
            if (ManifestResource.SERVICE.getPredicate().test(serviceSpec)) {
                logger.debug("Checking  for service ports in spec and adding service seletor labels to the snippet.");
                snippetList.add(getServiceSelectorLabels(serviceMetadata));
            }
        } catch (JsonProcessingException e) {
            logger.error("Error while serializing pod spec labels snippet ", e);
        }
        return snippetList;
    }

    private ManifestSnippet getServiceSelectorLabels(ServiceMetadata serviceMetadata)
            throws JsonProcessingException {
        ManifestSnippet selectorSnippet = new ManifestSnippet();
        selectorSnippet.setPath("spec.selector");
        selectorSnippet.setKind(ManifestResource.SERVICE.getKind());
        selectorSnippet.setSnippet(JsonSnippetConvertor.serialize(getSelectorLabels(serviceMetadata)));
        return selectorSnippet;
    }

    private ManifestSnippet getPodSpecSelectorLabels(ServiceMetadata serviceMetadata, String podSpecOwner)
            throws JsonProcessingException {
        ManifestSnippet selectorSnippet = new ManifestSnippet();
        selectorSnippet.setPath("spec.selector.matchLabels");
        selectorSnippet.setKind(podSpecOwner);
        selectorSnippet.setSnippet(JsonSnippetConvertor.serialize(getSelectorLabels(serviceMetadata)));
        return selectorSnippet;
    }

    private Map<String, String> getSelectorLabels(ServiceMetadata serviceMetadata) {
        return DefaultLabelBuilder.build(serviceMetadata);
    }

}
