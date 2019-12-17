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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.commons.utils.NormalizationUtil;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.GsonSnippetConvertor;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1ServicePort;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@ManifestPlugin(name = "PortsHandler")
public class PortsHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(PortsHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext)
            throws HyscaleException {
        if (serviceSpec == null) {
            throw new HyscaleException(CommonErrorCode.SERVICE_SPEC_REQUIRED);
        }
        TypeReference<List<Port>> listTypeReference = new TypeReference<List<Port>>() {
        };
        List<Port> portList = serviceSpec.get(HyscaleSpecFields.ports, listTypeReference);
        List<ManifestSnippet> manifestSnippetList = null;
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec)
                ? ManifestResource.STATEFUL_SET.getKind()
                : ManifestResource.DEPLOYMENT.getKind();
                
        if (portList == null || portList.isEmpty()) {
            return manifestSnippetList;
        }
        logger.debug("Processing container and service ports");
        manifestSnippetList = Lists.newArrayList();
        Set<V1ContainerPort> v1ContainerPorts = Sets.newHashSet();
        Set<V1ServicePort> v1ServicePorts = Sets.newHashSet();
        
        portList.stream().filter(port -> {
            return port != null && StringUtils.isNotBlank(port.getPort());
        }).forEach(each -> {
            /**
             * Building podSpec's container ports
             */
            V1ContainerPort v1ContainerPort = new V1ContainerPort();
            V1ServicePort v1ServicePort = new V1ServicePort();
            String[] portAndProtocol = each.getPort().split("/");
            String protocol = ServiceProtocol.TCP.name();
            if (portAndProtocol.length > 1) {
                protocol = ServiceProtocol.fromString(portAndProtocol[1]).name();
            }
            String portName = NormalizationUtil.normalize(portAndProtocol[0] + ManifestGenConstants.NAME_DELIMITER + protocol);
            v1ContainerPort.setProtocol(protocol);
            v1ServicePort.setProtocol(protocol);
            int portValue = Integer.valueOf(portAndProtocol[0]);
            v1ContainerPort.setContainerPort(portValue);
            v1ContainerPort.setName(portName);
            logger.debug("Processing container ports {}.",v1ContainerPort.getName());
            v1ContainerPorts.add(v1ContainerPort);
            
            /**
             * Building Service ports
             */
            v1ServicePort.setName(portName);
            logger.debug("Processing service ports {}.",v1ServicePort.getName());
            v1ServicePort.setPort(portValue);
            v1ServicePort.setTargetPort(new IntOrString(portValue));
            v1ServicePorts.add(v1ServicePort);
            logger.debug("Fetched container and service port.");
        });
        
        try {
            manifestSnippetList.add(buildServicePortsSnippet(v1ServicePorts, podSpecOwner));
            manifestSnippetList.add(buildContainerPortsSnippet(v1ContainerPorts, podSpecOwner));
        } catch (JsonProcessingException e) {
            logger.error("Error while building ports snippet {}", e);
        }
        return manifestSnippetList;
    }

    private ManifestSnippet buildContainerPortsSnippet(Set<V1ContainerPort> containerPorts, String podSpecOwner)
            throws JsonProcessingException {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.containers[0].ports");
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(containerPorts));
        return manifestSnippet;
    }


    private ManifestSnippet buildServicePortsSnippet(Set<V1ServicePort> servicePorts, String podSpecOwner)
            throws JsonProcessingException {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(ManifestResource.SERVICE.getKind());
        manifestSnippet.setPath("spec.ports");
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(servicePorts));
        return manifestSnippet;
    }

    public enum ServiceProtocol {
        TCP("tcp"), UDP("udp");

        private String protocolString;

        ServiceProtocol(String protocolString) {
            this.protocolString = protocolString;
        }

        public static ServiceProtocol fromString(String protocol) {
            if (StringUtils.isBlank(protocol)) {
                return TCP;
            }

            for (ServiceProtocol serviceProtocol : ServiceProtocol.values()) {
                if (serviceProtocol.getProtocolString().equalsIgnoreCase(protocol)) {
                    return serviceProtocol;
                }
            }
            return TCP;
        }

        public String getProtocolString() {
            return protocolString;
        }
    }
}
