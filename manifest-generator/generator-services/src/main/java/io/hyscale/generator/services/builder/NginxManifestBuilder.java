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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.commons.models.IngressRule;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class NginxManifestBuilder implements LoadBalancerBuilder {
    private static final Logger logger = LoggerFactory.getLogger(NginxManifestBuilder.class);
    private static String INGRESS_NAME = "INGRESS_NAME";
    private static String APP_NAME = "APP_NAME";
    private static String ENVIRONMENT_NAME = "ENVIRONMENT_NAME";

    private static String INGRESS_CLASS = "INGRESS_CLASS";
    private static String STICKY = "STICKY";
    private static String RULES = "RULES";

    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public List<ManifestSnippet> build(ManifestContext manifestContext, ServiceSpec serviceSpec, LoadBalancer loadBalancer) throws JsonProcessingException, HyscaleException {
        logger.debug("Building Manifests for Nginx Ingress Resource");
        //PluginTemplateProvider templateProvider = new PluginTemplateProvider();
        //templateProvider.init();
        ConfigTemplate nginxIngressTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.NGINX);
        //MustacheTemplateResolver templateResolver = new MustacheTemplateResolver();
        String yamlString = templateResolver.resolveTemplate(nginxIngressTemplate.getTemplatePath(), getContext(serviceSpec, manifestContext,loadBalancer));
        ManifestSnippet snippet = new ManifestSnippet();
        //TODO snippet.setKind() Set Ingress Resource
        snippet.setKind("Ingress");
        snippet.setPath("spec");
        snippet.setSnippet(yamlString);
        List<ManifestSnippet> snippetList = new LinkedList<>();
        snippetList.add(snippet);
        //return snippetList;
        return null;
    }

    private Map<String,Object> getContext(ServiceSpec serviceSpec, ManifestContext manifestContext, LoadBalancer loadBalancer) throws HyscaleException {
        Map<String, Object> context = new HashMap<>();
        String envName = manifestContext.getEnvName();
        String serviceName = serviceSpec.get(HyscaleSpecFields.name,String.class);
        context.put(INGRESS_NAME, envName+"-"+serviceName+"-ingress");
        context.put(APP_NAME,manifestContext.getAppName());
        context.put(ENVIRONMENT_NAME,envName);
        if(loadBalancer.getClassName()!= null && !loadBalancer.getClassName().isBlank()){
            context.put(INGRESS_CLASS,loadBalancer.getClassName());
        }
        context.put(STICKY,"cookie");
        //TODO build more context
        context.put(RULES,getIngressRules(serviceName,loadBalancer));
        return context;
    }

    private List<IngressRule> getIngressRules(String serviceName, LoadBalancer loadBalancer){
        List<IngressRule> ingressRules = new ArrayList<>();
        String host = loadBalancer.getHost();
        if(loadBalancer.getMapping() != null && !loadBalancer.getMapping().isEmpty()){
            loadBalancer.getMapping().forEach((mapping -> {
                String port = mapping.getPort();
                IngressRule ingressRule = new IngressRule();
                ingressRule.setHost(host);
                ingressRule.setRule(serviceName,port,mapping.getContextPaths());
                ingressRules.add(ingressRule);
            }));
        }
        return ingressRules;
    }

}
