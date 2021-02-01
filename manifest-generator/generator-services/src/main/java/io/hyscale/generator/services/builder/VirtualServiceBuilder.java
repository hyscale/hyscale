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
package io.hyscale.generator.services.builder;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VirtualServiceBuilder implements IstioResourcesManifestGenerator {

    private static final String DEFAULT_MATCH_TYPE = "prefix";

    private static final String MATCH_TYPE = "MATCH_TYPE";

    private static final String GATEWAYS = "gateways";

    private static final String HEADERS = "headers";


    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public ManifestSnippet generateManifest(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        ConfigTemplate virtualServiceTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.ISTIO_VIRTUAL_SERVICE);
        String yaml = templateResolver.resolveTemplate(virtualServiceTemplate.getTemplatePath(), getContext(serviceMetadata, loadBalancer));
        ManifestSnippet snippet = new ManifestSnippet();
        snippet.setKind(ManifestResource.VIRTUAL_SERVICE.getKind());
        snippet.setPath("spec");
        snippet.setSnippet(yaml);
        return snippet;
    }

    private Map<String, Object> getContext(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) {
        Map<String, Object> map = new HashMap<>();
        String serviceName = serviceMetadata.getServiceName();
        String envName = serviceMetadata.getEnvName();
        List<String> hosts = new ArrayList<>();
        hosts.add(loadBalancer.getHost());
        map.put(ManifestGenConstants.HOSTS, hosts);
        List<String> gateways = new ArrayList<>();
        gateways.add(getGatewayName(serviceName, envName));
        map.put(GATEWAYS, gateways);
        map.put(HEADERS, loadBalancer.getHeaders().entrySet());
        map.put(ManifestGenConstants.LOADBALANCER, loadBalancer);
        map.put("serviceName", serviceMetadata.getServiceName());
        map.put(MATCH_TYPE, DEFAULT_MATCH_TYPE);
        return map;
    }

    public String getGatewayName(String serviceName, String envName) {
        return (envName != null ? envName + ManifestGenConstants.NAME_DELIMITER : "") + serviceName + ManifestGenConstants.NAME_DELIMITER + ManifestGenConstants.ISTIO + ManifestGenConstants.NAME_DELIMITER + ManifestGenConstants.GATEWAY;
    }
}
