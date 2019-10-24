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

import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.K8sServiceType;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
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

            Boolean external = serviceSpec.get(HyscaleSpecFields.external, Boolean.class);
            K8sServiceType serviceType = getServiceType(external == null ? false : external);
            String serviceTypeName = serviceType != null ? serviceType.name() : K8sServiceType.ClusterIP.name();
            logger.debug("Processing serviceType {}.",serviceTypeName);
            serviceTypeSnippet.setSnippet(serviceTypeName);
            manifestSnippetList.add(serviceTypeSnippet);
        }
        return manifestSnippetList;
    }

    private K8sServiceType getServiceType(Boolean external) {

        if (external) {
            if (checkForLoadBalancerType()) {
                return K8sServiceType.LoadBalancer;
            } else {
                return K8sServiceType.NodePort;
            }
        } else {
            return K8sServiceType.ClusterIP;
        }
    }

    private boolean checkForLoadBalancerType() {
        return true;
    }

}
