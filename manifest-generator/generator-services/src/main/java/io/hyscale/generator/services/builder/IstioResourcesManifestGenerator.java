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
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * This class generates Manifests of Istio resources using the respective template.
 */
public abstract class IstioResourcesManifestGenerator {

    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    ManifestSnippet generateManifest(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        ConfigTemplate gatewayTemplate = templateProvider.get(getTemplateType());
        Map<String, Object> context = getContext(serviceMetadata, loadBalancer);
        if (context == null) {
            return null;
        }
        String yaml = templateResolver.resolveTemplate(gatewayTemplate.getTemplatePath(), context);
        ManifestSnippet snippet = new ManifestSnippet();
        snippet.setKind(getKind());
        snippet.setPath(getPath());
        snippet.setSnippet(yaml);
        return snippet;
    }

    protected abstract PluginTemplateProvider.PluginTemplateType getTemplateType();

    protected abstract String getKind();

    protected abstract String getPath();

    protected abstract Map<String, Object> getContext(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer);


}
