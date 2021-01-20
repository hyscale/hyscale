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
import io.hyscale.commons.models.*;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import me.snowdrop.istio.api.networking.v1beta1.Port;
import me.snowdrop.istio.api.networking.v1beta1.Server;
import me.snowdrop.istio.api.networking.v1beta1.ServerTLSSettings;
import me.snowdrop.istio.api.networking.v1beta1.ServerTLSSettingsMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GatewayBuilder implements IstioResourcesManifestGenerator{

    private static final String GATEWAY_NAME="GATEWAY_NAME";
    private static final String GATEWAY_LABELS = "labels";
    private static final String SERVERS = "servers";

    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public ManifestSnippet generateManifest(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        ConfigTemplate gatewayTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.ISTIO_GATEWAY);
        templateResolver.resolveTemplate(gatewayTemplate.getTemplatePath(), getContext(serviceMetadata,loadBalancer));
        //Generate Manifest snippet from the template.
        return null;
    }

    private Map<String, Object> getContext(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        Map<String, Object> map = new HashMap<>();
        String serviceName = serviceMetadata.getServiceName();
        String envName = serviceMetadata.getEnvName();
        map.put(GATEWAY_NAME, getGatewayName(serviceName,envName));
        map.put(GATEWAY_LABELS,loadBalancer.getLabels().entrySet());
        map.put(SERVERS,prepareServerProperties(loadBalancer));
        return map;
    }

    public String getGatewayName(String serviceName, String envName) {
        return serviceName + ToolConstants.DASH + (envName != null ? envName + ToolConstants.DASH : "") + ManifestGenConstants.ISTIO + ToolConstants.DASH + ManifestGenConstants.GATEWAY;
    }

    public List<Server> prepareServerProperties(LoadBalancer loadBalancer){
        List<Server> servers = new ArrayList<>();
        for(LoadBalancerMapping mapping: loadBalancer.getMapping()){
            Server server = new Server();
            List<String> hosts = new ArrayList<>();
            hosts.add(loadBalancer.getHost());
            server.setHosts(hosts);
            Port port = new Port();
            port.setName("default-port");
            int portNumber = Integer.parseInt(mapping.getPort().substring(0,mapping.getPort().indexOf('/')));
            port.setNumber(portNumber);
            port.setProtocol(mapping.getPort().substring(mapping.getPort().indexOf('/')+1));
            server.setPort(port);
            ServerTLSSettings tls = new ServerTLSSettings();
            tls.setMode(ServerTLSSettingsMode.SIMPLE);
            tls.setCredentialName(loadBalancer.getTlsSecret());
            server.setTls(tls);
            servers.add(server);
        }
        return servers;
    }
}
