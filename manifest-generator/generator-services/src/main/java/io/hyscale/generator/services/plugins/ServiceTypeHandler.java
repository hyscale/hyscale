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

import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.K8sServiceType;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "ServiceTypeHandler")
public class ServiceTypeHandler implements ManifestHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServiceTypeHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext)
            throws HyscaleException {
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        if (ManifestResource.SERVICE.getPredicate().test(serviceSpec)) {
            ManifestSnippet serviceTypeSnippet = new ManifestSnippet();
            serviceTypeSnippet.setKind(ManifestResource.SERVICE.getKind());
            serviceTypeSnippet.setPath("spec.type");
            K8sServiceType serviceType = getServiceType(serviceSpec);
            String serviceTypeName = serviceType != null ? serviceType.getName() : K8sServiceType.CLUSTER_IP.getName();
            logger.debug("Processing Service Type {}.",serviceTypeName);
            serviceTypeSnippet.setSnippet(serviceTypeName);
            manifestSnippetList.add(serviceTypeSnippet);
        }
        return manifestSnippetList;
    }

    /**
     * If loadBalancer is configured and external is specified as true in hspec, then Service type could be CLUSTER_IP.
     * If loadBalancer is not configured and external is true then service type could be LOAD_BALANCER.
     */
    private K8sServiceType getServiceType(ServiceSpec serviceSpec) throws HyscaleException {
        Boolean external = serviceSpec.get(HyscaleSpecFields.external, Boolean.class);
        if (BooleanUtils.toBoolean(external)) {
            if (checkForLoadBalancer(serviceSpec)) {
                return K8sServiceType.CLUSTER_IP;
            } else {
                return K8sServiceType.LOAD_BALANCER;
            }
        } else {
            return K8sServiceType.CLUSTER_IP;
        }
    }

    private boolean checkForLoadBalancer(ServiceSpec serviceSpec) throws HyscaleException {
        LoadBalancer loadBalancer = serviceSpec.get(HyscaleSpecFields.loadBalancer, LoadBalancer.class);
        return loadBalancer != null;
    }

}
