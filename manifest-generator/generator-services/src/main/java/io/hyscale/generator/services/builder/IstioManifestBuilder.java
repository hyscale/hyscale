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
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for creating manifests of Istio resources(Gateway,Virtual Service, Destination rule) for the load balancer configuration provided in hspec.
 */
@Component
public class IstioManifestBuilder implements LoadBalancerBuilder {
    private static final Logger logger = LoggerFactory.getLogger(IstioManifestBuilder.class);

    @Autowired
    private VirtualServiceBuilder virtualServiceBuilder;

    @Autowired
    private GatewayBuilder gatewayBuilder;

    @Autowired
    private DestinationRuleBuilder destinationRuleBuilder;

    List<IstioResourcesManifestGenerator> istioManifestGenerators;

    @PostConstruct
    public void init() {
        this.istioManifestGenerators = new ArrayList<>();
        istioManifestGenerators.add(virtualServiceBuilder);
        istioManifestGenerators.add(gatewayBuilder);
        istioManifestGenerators.add(destinationRuleBuilder);
    }

    @Override
    public List<ManifestSnippet> build(ServiceMetadata serviceMetadata, LoadBalancer loadBalancer) throws HyscaleException {
        logger.debug("Building Manifests for Istio LB Resources");
        List<ManifestSnippet> manifestSnippets = new ArrayList<>();
        for(IstioResourcesManifestGenerator istioManifestGenerator: istioManifestGenerators){
            manifestSnippets.add(istioManifestGenerator.generateManifest(serviceMetadata, loadBalancer));
        }
        return manifestSnippets;
    }
}
