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
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@Component
public class NginxMetaDataBuilder implements IngressMetaDataBuilder {
    private static String INGRESS_CLASS = "INGRESS_CLASS";
    private static String STICKY = "STICKY";

    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public ManifestSnippet build(LoadBalancer loadBalancer) throws HyscaleException {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(ManifestResource.INGRESS.getKind());
        manifestSnippet.setPath("metadata");
        ConfigTemplate nginxIngressTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.NGINX);
        String yamlString = templateResolver.resolveTemplate(nginxIngressTemplate.getTemplatePath(), getContext(loadBalancer));
        manifestSnippet.setSnippet(yamlString);
        return manifestSnippet;
    }

    private Map<String,Object> getContext(LoadBalancer loadBalancer){
        Map<String, Object> context = new HashMap<>();
        if(loadBalancer.getClassName()!= null && !loadBalancer.getClassName().isBlank()){
            context.put(INGRESS_CLASS,loadBalancer.getClassName());
        }
        if(loadBalancer.isSticky()){
            context.put(STICKY,"cookie");
        }
        return context;
    }
}
