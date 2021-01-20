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
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DestinationRuleSpecBuilder implements IstioResourcesManifestGenerator {

    private static final String DESTINATION_RULE_NAME="DESTINATION_RULE_NAME";
    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public ManifestSnippet generateManifest(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        ConfigTemplate virtualServiceTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.ISTIO_DESTINATION_RULE);
        templateResolver.resolveTemplate(virtualServiceTemplate.getTemplatePath(), getContext(serviceMetadata,loadBalancer));
        return null;
    }

    private Map<String, Object> getContext(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        Map<String, Object> map = new HashMap<>();
        //Need to build entire context.
        map.put(DESTINATION_RULE_NAME,serviceMetadata.getServiceName()+serviceMetadata.getEnvName());
        map.put("loadBalancer",loadBalancer);
        return map;
    }
}
