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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class generates  istio gateway yaml.
 */
@Component
public class GatewayBuilder extends IstioResourcesManifestGenerator {

    private static final String GATEWAY_LABELS = "labels";
    private static final String TLS_MODE = "TLS_MODE";
    private static final String DEFAULT_TLS_MODE = "SIMPLE";
    private static final String PROTOCOL = "PROTOCOL";
    private static final String PORT_NUMBER = "PORT_NUMBER";
    private static final Integer DEFAULT_GATEWAY_PORT = 80;
    private static final Integer DEFAULT_GATEWAY_TLS_PORT = 443;


    @Override
    protected PluginTemplateProvider.PluginTemplateType getTemplateType() {
        return PluginTemplateProvider.PluginTemplateType.ISTIO_GATEWAY;
    }

    @Override
    protected String getKind() {
        return ManifestResource.GATEWAY.getKind();
    }

    @Override
    protected String getPath() {
        return "spec";
    }

    @Override
    protected Map<String, Object> getContext(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) {
        Map<String, Object> map = new HashMap<>();
        map.put(GATEWAY_LABELS, loadBalancer.getLabels().entrySet());
        map.put(ManifestGenConstants.LOADBALANCER, loadBalancer);
        List<String> hosts = new ArrayList<>();
        hosts.add(loadBalancer.getHost());
        map.put(ManifestGenConstants.HOSTS, hosts);
        map.put(TLS_MODE, DEFAULT_TLS_MODE);
        map.put(PORT_NUMBER, loadBalancer.getTlsSecret() == null ? DEFAULT_GATEWAY_PORT : DEFAULT_GATEWAY_TLS_PORT);
        map.put(PROTOCOL, loadBalancer.getTlsSecret() != null ? "HTTPS" : "HTTP");
        return map;
    }
}
