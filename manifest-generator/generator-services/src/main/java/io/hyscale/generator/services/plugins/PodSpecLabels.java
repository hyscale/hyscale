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
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.model.MetaDataContext;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.models.V1ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "PodSpecLabels")
public class PodSpecLabels implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(PodSpecLabels.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setAppName(manifestContext.getAppName());
        metaDataContext.setEnvName(manifestContext.getEnvName());
        metaDataContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();
        try {
            ManifestSnippet metaDataSnippet = new ManifestSnippet();
            metaDataSnippet.setPath("spec.template.metadata");
            metaDataSnippet.setKind(podSpecOwner);
            metaDataSnippet.setSnippet(JsonSnippetConvertor.serialize(getTemplateMetaData(metaDataContext, podSpecOwner)));
            snippetList.add(metaDataSnippet);

        } catch (JsonProcessingException e) {
            logger.error("Error while serializing pod spec labels snippet ", e);
        }
        return snippetList;
    }

    private V1ObjectMeta getTemplateMetaData(MetaDataContext metaDataContext, String podSpecOwner) {
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        ManifestResource manifestResource = ManifestResource.fromString(podSpecOwner);
        if (manifestResource == null) {
            return null;
        }
        //TODO Add release-version ??
        v1ObjectMeta.setLabels(manifestResource.getLabels(metaDataContext));
        return v1ObjectMeta;
    }
}
