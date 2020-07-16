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

import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Replicas;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@ManifestPlugin(name = "ReplicasHandler")
public class ReplicasHandler implements ManifestHandler {
    private static final Logger logger = LoggerFactory.getLogger(ReplicasHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        Replicas replicas = serviceSpec.get(HyscaleSpecFields.replicas, Replicas.class);
        String podSpecOwner = (String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER);
        if (replicas == null || !(podSpecOwner.equals(ManifestResource.DEPLOYMENT.getKind()) || podSpecOwner.equals(ManifestResource.STATEFUL_SET.getKind()))) {
            logger.debug("Cannot handle replicas as the field is not declared");
            return Collections.emptyList();
        }
        // If user does not specify replicas field in hspec, by default we consider a single replica
        int replicaCount = replicas.getMin() >= 0 ? replicas.getMin() : 1;
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        ManifestSnippet replicaSnippet = new ManifestSnippet();
        replicaSnippet.setSnippet(String.valueOf(replicaCount));
        replicaSnippet.setPath("spec.replicas");
        replicaSnippet.setKind(podSpecOwner);

        manifestSnippetList.add(replicaSnippet);
        return manifestSnippetList;
    }
}
