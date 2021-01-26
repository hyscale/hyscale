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
import io.hyscale.commons.models.*;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.model.IngressProvider;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class IngressManifestBuilder implements LoadBalancerBuilder {
    private static final Logger logger = LoggerFactory.getLogger(IngressManifestBuilder.class);
    private static String RULES = "rules";
    private static String LOADBALANCER = "loadBalancer";
    private static String HOST = "host";
    private static String SERVICENAME = "serviceName";

    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public List<ManifestSnippet> build(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        logger.debug("Building Manifests for Ingress Resource");
        ConfigTemplate nginxIngressTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.INGRESS);
        String yamlString = templateResolver.resolveTemplate(nginxIngressTemplate.getTemplatePath(), getIngressSpecContext(serviceMetadata,loadBalancer));
        ManifestSnippet snippet = new ManifestSnippet();
        snippet.setKind(ManifestResource.INGRESS.getKind());
        snippet.setPath("spec");
        snippet.setSnippet(yamlString);
        List<ManifestSnippet> snippetList = new LinkedList<>();
        snippetList.add(snippet);
        snippetList.add(getProviderSpecificMetadata(serviceMetadata,loadBalancer));
        return snippetList;
    }

    private ManifestSnippet getProviderSpecificMetadata(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        String provider = loadBalancer.getProvider();
        return IngressProvider.fromString(provider).getMetadataBuilder().build(serviceMetadata,loadBalancer);
    }

    private Map<String,Object> getIngressSpecContext(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        Map<String, Object> context = new HashMap<>();
        String serviceName = serviceMetadata.getServiceName();
        context.put(LOADBALANCER,loadBalancer);
        context.put(SERVICENAME,serviceName);
        context.put(HOST,loadBalancer.getHost());
        return context;
    }
}
