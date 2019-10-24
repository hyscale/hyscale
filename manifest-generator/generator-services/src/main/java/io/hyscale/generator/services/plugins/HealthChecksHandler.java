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
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.GsonSnippetConvertor;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.models.V1HTTPGetAction;
import io.kubernetes.client.models.V1Probe;
import io.kubernetes.client.models.V1TCPSocketAction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@ManifestPlugin(name = "HealthChecksHandler")
public class HealthChecksHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(HealthChecksHandler.class);

    private static final int DEFAULT_INITIAL_DELAY_IN_SECONDS = 10;
    private static final int DEFAULT_PERIOD_IN_SECONDS = 30;
    private static final int DEFAULT_TIMEOUT_IN_SECONDS = 30;
    private static final int DEFAULT_FAILURE_THRESHOLD_IN_SECONDS = 10;
    private static final int DEFAULT_SUCESS_THRESHOLD_IN_SECONDS = 1;

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException {
        TypeReference<List<Port>> listTypeReference = new TypeReference<List<Port>>() {
        };
        List<Port> portsList = serviceSpec.get(HyscaleSpecFields.ports, listTypeReference);

        if (portsList == null || portsList.isEmpty()) {
            logger.debug("Cannot handle healthchecks as ports are empty.");
            return null;
        }
        // TODO supporiting single healthcheck
        boolean healthCheck = false;
        V1Probe v1Probe = new V1Probe();
        // TODO set successfull threshold

        Optional<Port> httpHealthCheckPort = portsList.stream().filter(each -> {
            return each.getPort() != null && each.getHealthCheck() != null && each.getHealthCheck().getHttpPath() != null;
        }).findFirst();

        if (httpHealthCheckPort.isPresent()) {
            Port port = httpHealthCheckPort.get();
            String[] portAndProtocol = port.getPort().split("/");
            String path = port.getHealthCheck().getHttpPath();
            if (StringUtils.isNotBlank(path)) {
                logger.debug("Adding HTTP healthcheck for port {} .", port);
                V1HTTPGetAction v1HTTPGetAction = new V1HTTPGetAction();
                v1HTTPGetAction.setPort(new IntOrString(Integer.valueOf(portAndProtocol[0])));
                v1HTTPGetAction.setPath(path);
                v1Probe.setHttpGet(v1HTTPGetAction);
                healthCheck = true;
            }
        } else {
            for (Port port : portsList) {
                String[] portAndProtocol = port.getPort().split("/");
                int portValue = Integer.valueOf(portAndProtocol[0]);
                logger.debug("Port {}, healthCheck {}", port.getPort(), port.getHealthCheck());
                if (port.getHealthCheck() != null) {
                    logger.debug("Adding TCP healthcheck for port {} .", port);
                    V1TCPSocketAction v1TCPSocketAction = new V1TCPSocketAction();
                    v1TCPSocketAction.setPort(new IntOrString(portValue));
                    v1Probe.setTcpSocket(v1TCPSocketAction);
                    healthCheck = true;
                    break;
                }
            }
        }

        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        if (healthCheck) {
            v1Probe.setInitialDelaySeconds(DEFAULT_INITIAL_DELAY_IN_SECONDS);
            v1Probe.setPeriodSeconds(DEFAULT_PERIOD_IN_SECONDS);
            v1Probe.setTimeoutSeconds(DEFAULT_TIMEOUT_IN_SECONDS);
            v1Probe.setFailureThreshold(DEFAULT_FAILURE_THRESHOLD_IN_SECONDS);
            try {
                String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec)
                        ? ManifestResource.STATEFUL_SET.getKind()
                        : ManifestResource.DEPLOYMENT.getKind();
                manifestSnippetList.add(buildReadinessProbe(v1Probe, podSpecOwner));
                manifestSnippetList.add(buildLiveinessProbe(v1Probe, podSpecOwner));
                logger.debug("Processing healthchecks done.");
            } catch (JsonProcessingException e) {
                logger.error("Error while serializing health checks ", e);
            }
        }
        return manifestSnippetList;
    }

    private ManifestSnippet buildReadinessProbe(V1Probe v1Probe, String podSpecOwner) throws JsonProcessingException {
        if (v1Probe == null) {
            return null;
        }
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(v1Probe));
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.containers[0].readinessProbe");
        return manifestSnippet;
    }

    private ManifestSnippet buildLiveinessProbe(V1Probe v1Probe, String podSpecOwner) throws JsonProcessingException {
        if (v1Probe == null) {
            return null;
        }
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(v1Probe));
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.containers[0].livenessProbe");
        return manifestSnippet;
    }
}
