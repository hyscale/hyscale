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

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * Plugin for Custom Snippets
 * <p>
 * This class is responsible for patching custom k8s snippets on the generated manifests.
 * </p>
 *
 * @author Nishanth Panthangi
 */

@Component
@ManifestPlugin(name = "CustomK8sSnippetHandler")
public class CustomK8sSnippetHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(CustomK8sSnippetHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        if (!ManifestPredicates.getCustomSnippetsPredicate().test(serviceSpec)) {
            logger.debug("Custom Snippets found to be empty while processing service spec data.");
            return Collections.emptyList();
        }
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        List<String> k8sSnippetFilePaths= serviceSpec.get(HyscaleSpecFields.k8sPatches,
                new TypeReference<List<String>>() {} );

        return manifestSnippetList;
    }

}
