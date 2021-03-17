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
package io.hyscale.generator.services.plugins;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.LBType;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.generator.services.model.LBBuilderType;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Plugin for Generating the Manifests required for Load Balancing configuration specified in hspec.
 */
@Component
@ManifestPlugin(name = "LoadBalancerHandler")
public class LoadBalancerHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        if (!ManifestPredicates.getLoadBalancerPredicate(LBType.INGRESS).test(serviceSpec) && !ManifestPredicates.getLoadBalancerPredicate(LBType.ISTIO).test(serviceSpec)) {
            logger.debug("Load Balancer information found to be empty while processing service spec data.");
            return Collections.emptyList();
        }
        LoadBalancer loadBalancer = serviceSpec.get(HyscaleSpecFields.loadBalancer, LoadBalancer.class);
        LBType lbType = LBType.getByProvider(loadBalancer.getProvider());
        LBBuilderType lbBuilderType = LBBuilderType.getByType(lbType);
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setAppName(manifestContext.getAppName());
        serviceMetadata.setEnvName(manifestContext.getEnvName());
        String serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
        serviceMetadata.setServiceName(serviceName);
        return lbBuilderType != null ? lbBuilderType.getBuilder().build(serviceMetadata, loadBalancer) : Collections.emptyList();
    }
}
