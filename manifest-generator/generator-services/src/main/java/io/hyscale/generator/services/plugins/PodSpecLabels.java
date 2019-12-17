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
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.CommonErrorCode;
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
        if (serviceSpec == null) {
            throw new HyscaleException(CommonErrorCode.SERVICE_SPEC_REQUIRED);
        }
        if (manifestContext == null) {
            throw new HyscaleException(ManifestErrorCodes.CONTEXT_REQUIRED);
        }
        AppMetaData appMetaData = new AppMetaData();
        appMetaData.setAppName(manifestContext.getAppName());
        appMetaData.setEnvName(manifestContext.getEnvName());
        appMetaData.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();
        try {
            ManifestSnippet metaDataSnippet = new ManifestSnippet();
            metaDataSnippet.setPath("spec.template.metadata");
            metaDataSnippet.setKind(podSpecOwner);
            metaDataSnippet.setSnippet(JsonSnippetConvertor.serialize(getTemplateMetaData(appMetaData, podSpecOwner)));
            snippetList.add(metaDataSnippet);

        } catch (JsonProcessingException e) {
            logger.error("Error while serializing Pod spec labels snippet ", e);
        }
        return snippetList;    }

    private V1ObjectMeta getTemplateMetaData(AppMetaData appMetaData, String podSpecOwner) {
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setLabels(DefaultLabelBuilder.build(appMetaData));
        //TODO Add release-version ??
        return v1ObjectMeta;
    }
}
