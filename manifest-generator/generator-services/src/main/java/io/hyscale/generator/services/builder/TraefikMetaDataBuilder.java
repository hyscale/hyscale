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

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
/**
 * Builds Metadata for Traefik Ingress resource yaml
 */
@Component
public class TraefikMetaDataBuilder implements IngressMetaDataBuilder {

    private static final String INGRESS_NAME = "INGRESS_NAME";
    private static final String APP_NAME = "APP_NAME";
    private static final String ENV_NAME = "ENV_NAME";
    private static final String SERVICE_NAME = "SERVICE_NAME";
    private static final String INGRESS_CLASS = "INGRESS_CLASS";
    private static final String FRONTEND_ENTRY_POINTS = "FRONTEND_ENTRY_POINTS";
    private static final String REDIRECT_ENTRY_POINTS = "REDIRECT_ENTRY_POINTS";
    private static final String HEADERS_EXPRESSION = "HEADERS_EXPRESSION";

    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public ManifestSnippet build(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(ManifestResource.INGRESS.getKind());
        manifestSnippet.setPath("metadata");
        ConfigTemplate nginxIngressTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.TRAEFIK);
        String yamlString = templateResolver.resolveTemplate(nginxIngressTemplate.getTemplatePath(), getContext(serviceMetadata,loadBalancer));
        manifestSnippet.setSnippet(yamlString);
        return manifestSnippet;
    }

    private Map<String, Object> getContext(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) {
        Map<String, Object> context = new HashMap<>();
        context.put(INGRESS_NAME,ManifestResource.INGRESS.getName(serviceMetadata));
        context.put(APP_NAME,serviceMetadata.getAppName());
        context.put(SERVICE_NAME,serviceMetadata.getServiceName());
        context.put(ENV_NAME,serviceMetadata.getEnvName());
        if(loadBalancer.getClassName()!= null && !loadBalancer.getClassName().isBlank()){
            context.put(INGRESS_CLASS,loadBalancer.getClassName());
            context.put(FRONTEND_ENTRY_POINTS,"http,https");
            context.put(REDIRECT_ENTRY_POINTS,"https");
        }
        if(loadBalancer.getTlsSecret()!= null && !loadBalancer.getTlsSecret().isBlank()){
            context.put(FRONTEND_ENTRY_POINTS,"http");
            context.put(REDIRECT_ENTRY_POINTS,null);
        }
        if(loadBalancer.getHeaders()!=null ){
            StringBuilder sb = new StringBuilder();
            loadBalancer.getHeaders().forEach((key,value) -> sb.append(key).append(ToolConstants.COLON).append(value).append(ToolConstants.HEADER_CONCATENATOR));
            sb.deleteCharAt(sb.lastIndexOf("|"));
            context.put(HEADERS_EXPRESSION,sb);
        }
        return context;
    }
}
