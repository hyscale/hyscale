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

import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DestinationRuleBuilder extends IstioResourcesManifestGenerator {

    private static final String HOST_NAME = "HOST_NAME";


    @Override
    protected PluginTemplateProvider.PluginTemplateType getTemplateType() {
        return PluginTemplateProvider.PluginTemplateType.ISTIO_DESTINATION_RULE;
    }

    @Override
    protected String getKind() {
        return ManifestResource.DESTINATION_RULE.getKind();
    }

    @Override
    protected String getPath() {
        return "spec";
    }

    @Override
    protected Map<String, Object> getContext(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) {
        if(loadBalancer.isSticky()){
            Map<String, Object> map = new HashMap<>();
            map.put(HOST_NAME, serviceMetadata.getServiceName());
            map.put(ManifestGenConstants.LOADBALANCER, loadBalancer);
            return map;
        }
        return  null;
    }
}
