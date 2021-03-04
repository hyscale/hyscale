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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.GsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.Port;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.V1HTTPGetAction;
import io.kubernetes.client.openapi.models.V1Probe;
import io.kubernetes.client.openapi.models.V1TCPSocketAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DefaultHealthChecksBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHealthChecksBuilder.class);
    private static final int DEFAULT_INITIAL_DELAY_IN_SECONDS = 10;
    private static final int DEFAULT_PERIOD_IN_SECONDS = 30;
    private static final int DEFAULT_TIMEOUT_IN_SECONDS = 30;
    private static final int DEFAULT_FAILURE_THRESHOLD_IN_SECONDS = 10;
    private static final String HTTPS = "HTTPS";

    public List<ManifestSnippet> generateHealthCheckSnippets(List<Port> portsList, String podSpecOwner) {
        return generateHealthCheckSnippets(portsList, podSpecOwner, 0);
    }

    public List<ManifestSnippet> generateHealthCheckSnippets(List<Port> portsList, String podSpecOwner, int containerIndex) {

        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        if (portsList == null || portsList.isEmpty()) {
            logger.debug("Cannot handle HealthChecks as ports are empty.");
            return manifestSnippetList;
        }
        V1Probe v1Probe = getHealthProbe(portsList);
        if (v1Probe != null) {
            v1Probe.setInitialDelaySeconds(DEFAULT_INITIAL_DELAY_IN_SECONDS);
            v1Probe.setPeriodSeconds(DEFAULT_PERIOD_IN_SECONDS);
            v1Probe.setTimeoutSeconds(DEFAULT_TIMEOUT_IN_SECONDS);
            v1Probe.setFailureThreshold(DEFAULT_FAILURE_THRESHOLD_IN_SECONDS);
            try {
                manifestSnippetList.add(buildReadinessProbe(v1Probe, podSpecOwner, containerIndex));
                manifestSnippetList.add(buildLivelinessProbe(v1Probe, podSpecOwner, containerIndex));
                logger.debug("Processing HealthChecks done.");
            } catch (JsonProcessingException e) {
                logger.error("Error while serializing health checks ", e);
            }

        }
        return manifestSnippetList;
    }

    public V1Probe getHealthProbe(List<Port> portsList) {
        Optional<Port> httpHealthCheckPort = portsList.stream().filter(each -> each.getPort() != null
                && each.getHealthCheck() != null && each.getHealthCheck().getHttpPath() != null).findFirst();

        if (httpHealthCheckPort.isPresent()) {
            Port port = httpHealthCheckPort.get();
            String[] portAndProtocol = port.getPort().split("/");
            String path = port.getHealthCheck().getHttpPath();
            if (StringUtils.isNotBlank(path)) {
                String protocol = portAndProtocol.length > 1 ? portAndProtocol[1] : null;
                logger.debug("Protocol {} for HealthCheck for port {} .", protocol, port.getPort());
                V1HTTPGetAction v1HTTPGetAction = new V1HTTPGetAction();
                v1HTTPGetAction.setPort(new IntOrString(Integer.parseInt(portAndProtocol[0])));
                v1HTTPGetAction.setPath(path);
                if (HTTPS.equalsIgnoreCase(protocol)) {
                    v1HTTPGetAction.setScheme(HTTPS);
                }
                V1Probe v1Probe = new V1Probe();
                v1Probe.setHttpGet(v1HTTPGetAction);
                return v1Probe;
            }
            return null;
        }
        for (Port port : portsList) {
            String[] portAndProtocol = port.getPort().split("/");
            int portValue = Integer.parseInt(portAndProtocol[0]);
            logger.debug("Port {}, HealthCheck {}", port.getPort(), port.getHealthCheck());
            if (port.getHealthCheck() != null) {
                logger.debug("Adding TCP HealthCheck for port {} .", port);
                V1TCPSocketAction v1TCPSocketAction = new V1TCPSocketAction();
                v1TCPSocketAction.setPort(new IntOrString(portValue));
                V1Probe v1Probe = new V1Probe();
                v1Probe.setTcpSocket(v1TCPSocketAction);
                return v1Probe;
            }
        }
        return null;
    }

    public ManifestSnippet buildReadinessProbe(V1Probe v1Probe, String podSpecOwner, int containerIndex) throws JsonProcessingException {
        if (v1Probe == null) {
            return null;
        }
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(v1Probe));
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.containers[" + containerIndex + "].readinessProbe");
        return manifestSnippet;
    }

    public ManifestSnippet buildLivelinessProbe(V1Probe v1Probe, String podSpecOwner, int containerIndex) throws JsonProcessingException {
        if (v1Probe == null) {
            return null;
        }
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(v1Probe));
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.containers[" + containerIndex + "].livenessProbe");
        return manifestSnippet;
    }

}
