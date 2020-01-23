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
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.model.AppMetaData;
import io.hyscale.generator.services.predicates.ManifestPredicates;
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
        AppMetaData appMetaData = new AppMetaData();
        appMetaData.setAppName(manifestContext.getAppName());
        appMetaData.setEnvName(manifestContext.getEnvName());
        appMetaData.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        String podSpecOwner = ((ManifestResource) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER)).getKind();
        try {
            snippetList.add(getPodSpecSelectorLabels(appMetaData, podSpecOwner));
            if (ManifestResource.SERVICE.getPredicate().test(serviceSpec)) {
                logger.debug("Checking  for service ports in spec and adding service seletor labels to the snippet.");
                snippetList.add(getServiceSelectorLabels(appMetaData, podSpecOwner));
            }
        } catch (JsonProcessingException e) {
            logger.error("Error while serializing pod spec labels snippet ", e);
        }
        return snippetList;
    }

    private ManifestSnippet getServiceSelectorLabels(AppMetaData appMetaData, String podSpecOwner)
            throws JsonProcessingException {
        ManifestSnippet selectorSnippet = new ManifestSnippet();
        selectorSnippet.setPath("spec.selector");
        selectorSnippet.setKind(ManifestResource.SERVICE.getKind());
        selectorSnippet.setSnippet(JsonSnippetConvertor.serialize(getSelectorLabels(appMetaData, podSpecOwner)));
        return selectorSnippet;
    }

    private ManifestSnippet getPodSpecSelectorLabels(AppMetaData appMetaData, String podSpecOwner)
            throws JsonProcessingException {
        ManifestSnippet selectorSnippet = new ManifestSnippet();
        selectorSnippet.setPath("spec.selector.matchLabels");
        selectorSnippet.setKind(podSpecOwner);
        selectorSnippet.setSnippet(JsonSnippetConvertor.serialize(getSelectorLabels(appMetaData, podSpecOwner)));
        return selectorSnippet;
    }

    private Map<String, String> getSelectorLabels(AppMetaData appMetaData, String podSpecOwner) {
        ManifestResource manifestResource = ManifestResource.fromString(podSpecOwner);
        if (manifestResource == null) {
            return null;
        }
        return DefaultLabelBuilder.build(appMetaData);
    }

}
