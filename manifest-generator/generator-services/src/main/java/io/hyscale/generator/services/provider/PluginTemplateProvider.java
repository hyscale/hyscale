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
package io.hyscale.generator.services.provider;

import io.hyscale.commons.models.ConfigTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class PluginTemplateProvider {

    protected static final String TEMPLATES_PATH = "templates";

    private Map<String, ConfigTemplate> templateMap;

    @PostConstruct
    public void init() {
        templateMap = new HashMap<>();
        for (PluginTemplateType each : PluginTemplateType.values()) {
            ConfigTemplate configTemplate = new ConfigTemplate();
            configTemplate.setRootPath(TEMPLATES_PATH);
            configTemplate.setTemplateName(each.getTemplateFile());
            templateMap.put(each.getTemplateFile(), configTemplate);
        }
    }

    public ConfigTemplate get(PluginTemplateType type) {
        if (type == null) {
            return null;
        }
        return templateMap != null && templateMap.containsKey(type.getTemplateFile()) ? templateMap.get(type.getTemplateFile()) : null;
    }


    public enum PluginTemplateType {
        HPA("hpa.yaml.tpl"),

        INGRESS("/loadBalancer/ingress/ingress-spec.yaml.tpl"),

        NGINX("/loadBalancer/ingress/nginx/ingress-meta.yaml.tpl"),

        TRAEFIK("/loadBalancer/ingress/traefik/ingress-meta.yaml.tpl"),

        ISTIO_VIRTUAL_SERVICE("/loadBalancer/istio/virtualService.yaml.tpl"),

        ISTIO_DESTINATION_RULE("/loadBalancer/istio/destinationRule.yaml.tpl"),

        ISTIO_GATEWAY("/loadBalancer/istio/gateway.yaml.tpl"),

        NETWORK_POLICY("networkPolicy.yaml.tpl");
    		
        String templateFile;

        PluginTemplateType(String tplFile) {
            this.templateFile = tplFile;
        }

        public String getTemplateFile() {
            return templateFile;
        }
    }
}
