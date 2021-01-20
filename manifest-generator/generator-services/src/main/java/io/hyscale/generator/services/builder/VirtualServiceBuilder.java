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
import me.snowdrop.istio.api.networking.v1beta1.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class VirtualServiceBuilder implements IstioResourcesManifestGenerator{

    private static final String VIRTUAL_SERVICE_NAME ="VIRTUAL_SERVICE_NAME";

    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public ManifestSnippet generateManifest(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        ConfigTemplate virtualServiceTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.ISTIO_VIRTUAL_SERVICE);
        templateResolver.resolveTemplate(virtualServiceTemplate.getTemplatePath(), getContext(serviceMetadata,loadBalancer));
        //Generate Manifest snippet from the template
        return null;
    }

    private Map<String, Object> getContext(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        Map<String, Object> map = new HashMap<>();
        String serviceName = serviceMetadata.getServiceName();
        String envName = serviceMetadata.getEnvName();
        map.put(VIRTUAL_SERVICE_NAME, getVirtualServiceName(serviceName, envName));
        List<String> hosts = new ArrayList<>();
        hosts.add(loadBalancer.getHost());
        map.put("hosts", hosts);
        List<String> gateways = new ArrayList<>();
        gateways.add(getGatewayName(serviceName, envName));
        map.put("gateways", gateways);
        map.put("headers", loadBalancer.getHeaders().entrySet());
        map.put("matchRequests", prepareHTTPMatchRequests(loadBalancer.getMapping().get(0)));
        map.put("routes", prepareHTTPRouteDestination(loadBalancer.getMapping().get(0)));
        return map;
    }

    public String getVirtualServiceName(String serviceName, String envName) {
        return serviceName + ToolConstants.DASH + (envName != null ? envName + ToolConstants.DASH : "") + ManifestGenConstants.ISTIO + ToolConstants.DASH + ManifestGenConstants.VIRTUAL_SERVICE;
    }

    public String getGatewayName(String serviceName, String envName) {
        return serviceName + ToolConstants.DASH + (envName != null ? envName + ToolConstants.DASH : "") + ManifestGenConstants.ISTIO + ToolConstants.DASH + ManifestGenConstants.GATEWAY;
    }

    public List<HTTPMatchRequest> prepareHTTPMatchRequests(LoadBalancerMapping mapping) {
        List<HTTPMatchRequest> matchRequests = new ArrayList<>();
        for (String path : mapping.getContextPaths()) {
            HTTPMatchRequest matchRequest = new HTTPMatchRequest();
            PrefixMatchType matchType = new PrefixMatchType();
            matchType.setPrefix(path);
            StringMatch uri = new StringMatch();
            uri.setMatchType(matchType);
            matchRequest.setUri(uri);
            matchRequests.add(matchRequest);
        }
        return matchRequests;
    }

    public List<HTTPRouteDestination> prepareHTTPRouteDestination(LoadBalancerMapping mapping){
        List<HTTPRouteDestination> routeDestinations = new ArrayList<>();
        HTTPRouteDestination routeDestination = new HTTPRouteDestination();
        Destination destination = new Destination();
        destination.setHost("rating");
        PortSelector port = new PortSelector();
        port.setNumber(Integer.parseInt(mapping.getPort().substring(0,mapping.getPort().indexOf('/'))));
        destination.setPort(port);
        routeDestination.setDestination(destination);
        routeDestinations.add(routeDestination);
        return routeDestinations;
    }

}
