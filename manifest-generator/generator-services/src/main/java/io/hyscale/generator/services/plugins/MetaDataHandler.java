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
import io.hyscale.generator.services.model.AppMetaData;
import io.hyscale.generator.services.generator.MetadataManifestSnippetGenerator;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.models.V1ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "MetaDataHandler")
public class MetaDataHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(MetaDataHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException {
        AppMetaData appMetaData = new AppMetaData();
        appMetaData.setAppName(context.getAppName());
        appMetaData.setEnvName(context.getEnvName());
        appMetaData.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        try {
            for (ManifestResource manifestResource : ManifestResource.values()) {

                if (manifestResource.getPredicate().test(serviceSpec)) {
                    logger.debug("Creating metadata for resource {}.",manifestResource.getKind());
                    /* Snippet for kind for each manifest */
                    snippetList.add(MetadataManifestSnippetGenerator.getKind(manifestResource));

                    /* Snippet for apiVersion for each manifest */
                    snippetList.add(MetadataManifestSnippetGenerator.getApiVersion(manifestResource, appMetaData));

                    /* Snippet for metadata for each manifest */
                    snippetList.add(MetadataManifestSnippetGenerator.getMetaData(manifestResource, appMetaData));

                }

            }
        } catch (JsonProcessingException e) {
            logger.error("Error while serializing metadata snippet.", e);
        }
        return snippetList;
    }


}
