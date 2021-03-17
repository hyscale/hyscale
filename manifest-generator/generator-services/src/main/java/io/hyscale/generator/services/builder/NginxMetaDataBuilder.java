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
 * Builds Metadata for  Nginx Ingress resource yaml
 */
@Component
public class NginxMetaDataBuilder implements IngressMetaDataBuilder {
    private static final String INGRESS_CLASS = "INGRESS_CLASS";
    private static final String STICKY = "STICKY";
    private static final String INGRESS_NAME = "INGRESS_NAME";
    private static final String APP_NAME = "APP_NAME";
    private static final String ENV_NAME = "ENV_NAME";
    private static final String SERVICE_NAME = "SERVICE_NAME";
    private static final String SSL_REDIRECT = "SSL_REDIRECT";
    private static final String CONFIGURATION_SNIPPET = "CONFIGURATION_SNIPPET";
    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public ManifestSnippet build(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(ManifestResource.INGRESS.getKind());
        manifestSnippet.setPath("metadata");
        ConfigTemplate nginxIngressTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.NGINX);
        String yamlString = templateResolver.resolveTemplate(nginxIngressTemplate.getTemplatePath(), getContext(serviceMetadata,loadBalancer));
        manifestSnippet.setSnippet(yamlString);
        return manifestSnippet;
    }

    private Map<String,Object> getContext(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer){
        Map<String, Object> context = new HashMap<>();
        context.put(INGRESS_NAME,ManifestResource.INGRESS.getName(serviceMetadata));
        context.put(APP_NAME,serviceMetadata.getAppName());
        context.put(ENV_NAME,serviceMetadata.getEnvName());
        context.put(SERVICE_NAME,serviceMetadata.getServiceName());
        if(loadBalancer.getClassName()!= null && !loadBalancer.getClassName().isBlank()){
            context.put(INGRESS_CLASS,loadBalancer.getClassName());
        }
        if(loadBalancer.isSticky()){
            context.put(STICKY,"cookie");
        }
        if(loadBalancer.getTlsSecret()!= null && !loadBalancer.getTlsSecret().isBlank()){
            context.put(SSL_REDIRECT,"true");
        }
        if(loadBalancer.getHeaders()!= null && !loadBalancer.getHeaders().isEmpty()){
            context.put(CONFIGURATION_SNIPPET, getConfigurationSnippetAkaHeaders(loadBalancer.getHeaders()));
        }
        return context;
    }

    private String getConfigurationSnippetAkaHeaders(Map<String,String> headers){
        StringBuilder configurationSnippet = new StringBuilder();
        headers.forEach((key, value) -> configurationSnippet.append("proxy_set_header").append(ToolConstants.SPACE).
                append(key).append(ToolConstants.SPACE).append(value).append(ToolConstants.SEMI_COLON));
        return configurationSnippet.toString();
    }
}
