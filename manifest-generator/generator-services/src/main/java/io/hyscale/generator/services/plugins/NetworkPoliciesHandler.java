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
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
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

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class generates manifests to apply
 * the network policy rules
 * provided in the service spec.
 */

@Component
@ManifestPlugin(name = "NetworkPoliciesHandler")
public class NetworkPoliciesHandler implements ManifestHandler {

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Autowired
    private PluginTemplateProvider templateProvider;

    private static final Logger logger = LoggerFactory.getLogger(NetworkPoliciesHandler.class);
    private static final String SERVICE_NAME = "service_name";
    private static final String NETWORK_TRAFFIC_RULES = "rules";
    private static final String ENABLE_TRAFFIC = "enable";

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        // Check if the Spec is External
        if (!ManifestPredicates.isNetworkPolicyEnabled().test(serviceSpec)) {
            return Collections.emptyList();
        }
        try {
            // All Network Traffic Rules Go Under Allow Traffic
            List<NetworkTrafficRule> networkTrafficRules = serviceSpec.get(HyscaleSpecFields.allowTraffic, new TypeReference<>() {
            });

            // Only Port Number required to generate traffic rules
            networkTrafficRules.stream().forEach(rule -> rule
                    .setPorts(rule.getPorts().stream().map(port -> port.split(ToolConstants.PORTS_PROTOCOL_SEPARATOR)[0]).collect(Collectors.toList())));

            // To Generate k8s YAML Template
            logger.info("Started Network Policies Handler");
            ConfigTemplate networkPoliciesTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.NETWORK_POLICY);
            String yamlString = templateResolver.resolveTemplate(networkPoliciesTemplate.getTemplatePath(), getContext(networkTrafficRules, serviceSpec));
            ManifestSnippet snippet = new ManifestSnippet();
            snippet.setKind(ManifestResource.NETWORK_POLICY.getKind());
            snippet.setPath("spec");
            snippet.setSnippet(yamlString);

            List<ManifestSnippet> snippetList = new LinkedList<>();
            snippetList.add(snippet);
            logger.info("Successfully generated manifest for Network Policy");
            return snippetList;
        } catch (Exception e) {
            HyscaleException ex = new HyscaleException(e, ManifestErrorCodes.ERROR_WHILE_CREATING_MANIFEST);
            logger.error("Error while generating Manifest Files", ex);
            throw ex;
        }
    }

    public Map<String, Object> getContext(List<NetworkTrafficRule> networkTrafficRules, ServiceSpec serviceSpec) throws HyscaleException {
        Map<String, Object> context = new HashMap<>();
        String serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
        context.put(SERVICE_NAME, serviceName);
        if (!networkTrafficRules.isEmpty()) {
            networkTrafficRules.removeIf(rule -> rule.getFrom() == null);
        }
        context.put(ENABLE_TRAFFIC, networkTrafficRules.isEmpty());
        context.put(NETWORK_TRAFFIC_RULES, networkTrafficRules);
        return context;
    }
}