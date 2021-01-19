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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.LBType;
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

@Component
@ManifestPlugin(name = "LoadBalancerHandler")
public class LoadBalancerHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerHandler.class);

    /**
     *
     * @param serviceSpec
     * @param manifestContext
     * @return
     * @throws HyscaleException
     */
    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        if (!ManifestPredicates.getLoadBalancerPredicate().test(serviceSpec)) {
            logger.debug("Load Balancer information found to be empty while processing service spec data.");
            return Collections.emptyList();
        }
        LoadBalancer loadBalancer = serviceSpec.get(HyscaleSpecFields.loadBalancer, new TypeReference<LoadBalancer>(){});
        try {
            LBType lbType = LBType.getByProvider(loadBalancer.getProvider());
            if(lbType != null){
                return lbType.getBuilder().build(manifestContext,serviceSpec,loadBalancer);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
