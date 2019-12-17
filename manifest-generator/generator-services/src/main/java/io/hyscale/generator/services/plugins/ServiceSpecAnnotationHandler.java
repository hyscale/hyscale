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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
@ManifestPlugin(name = "ServiceSpecAnnotationHandler")
public class ServiceSpecAnnotationHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServiceSpecAnnotationHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        if (serviceSpec == null) {
            throw new HyscaleException(CommonErrorCode.SERVICE_SPEC_REQUIRED);
        }
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec)
                ? ManifestResource.STATEFUL_SET.getKind()
                : ManifestResource.DEPLOYMENT.getKind();
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("metadata.annotations");

        Map<String, String> annotations = new HashMap<>();
        try {
            Secrets secrets = serviceSpec.get(HyscaleSpecFields.secrets, Secrets.class);
            if (secrets != null ) {
                ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
                ObjectNode serviceSpecNode = mapper.readValue(serviceSpec.toString(), ObjectNode.class);
                serviceSpecNode.remove(HyscaleSpecFields.secrets);
                annotations.put(AnnotationKey.HYSCALE_SERVICE_SPEC.getAnnotation(), mapper.writeValueAsString(serviceSpecNode));
            } else {
                annotations.put(AnnotationKey.HYSCALE_SERVICE_SPEC.getAnnotation(), serviceSpec.toString());
            }
            manifestSnippet.setSnippet(JsonSnippetConvertor.serialize(annotations));
            manifestSnippetList.add(manifestSnippet);
            return manifestSnippetList;
        } catch (IOException e) {
            logger.error("Error while processing service spec annotations");
            return null;
        }
    }
}
