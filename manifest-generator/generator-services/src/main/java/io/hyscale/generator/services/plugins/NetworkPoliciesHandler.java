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
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.NetworkTrafficRule;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
@ManifestPlugin(name = "NetworkPoliciesHandler")
public class NetworkPoliciesHandler implements ManifestHandler {

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Autowired
    private PluginTemplateProvider templateProvider;

    private static final Logger logger = LoggerFactory.getLogger(NetworkPoliciesHandler.class);
    private static final String SERVICE_NAME = "service_name";
    private static final String NETWORK_RULES = "rules";

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        // Check if the Spec is External
        if (serviceSpec.get(HyscaleSpecFields.external) == null || serviceSpec.get(HyscaleSpecFields.external, Boolean.class)) {
            return null;
        }
        try {
            // All Network Traffic Rules Go Under Allow Traffic
            List<NetworkTrafficRule> networkTrafficRules = serviceSpec.get(HyscaleSpecFields.allowTraffic, new TypeReference<>() {
            });

            // To Generate k8s YAML Template
            ConfigTemplate networkPoliciesTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.NETWORK_POLICY);
            String yamlString = templateResolver.resolveTemplate(networkPoliciesTemplate.getTemplatePath(), getContext(networkTrafficRules, serviceSpec, manifestContext));

            ManifestSnippet snippet = new ManifestSnippet();
            snippet.setKind(ManifestResource.NETWORK_POLICY.getKind());
            snippet.setPath("spec");
            snippet.setSnippet(yamlString);

            List<ManifestSnippet> snippetList = new LinkedList<>();
            snippetList.add(snippet);
            return snippetList;
        } catch (Exception e) {
            logger.info("Error while Generating Snippet", e);
            return null;
        }
    }

    public Map<String, Object> getContext(List<NetworkTrafficRule> networkTrafficRules, ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        Map<String, Object> context = new HashMap<>();
        String serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
        context.put(SERVICE_NAME, serviceName);
        context.put(NETWORK_RULES, networkTrafficRules);
        return context;
    }
}