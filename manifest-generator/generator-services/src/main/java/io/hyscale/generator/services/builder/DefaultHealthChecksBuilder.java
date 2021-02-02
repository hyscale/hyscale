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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultHealthChecksBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHealthChecksBuilder.class);
    private static final int DEFAULT_INITIAL_DELAY_IN_SECONDS = 10;
    private static final int DEFAULT_PERIOD_IN_SECONDS = 30;
    private static final int DEFAULT_TIMEOUT_IN_SECONDS = 30;
    private static final int DEFAULT_FAILURE_THRESHOLD_IN_SECONDS = 10;
    private static final String HTTPS = "HTTPS";

    public static List<ManifestSnippet> generateHealthCheckSnippets(List<Port> portsList, String podSpecOwner) {

        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        if (portsList == null || portsList.isEmpty()) {
            logger.debug("Cannot handle HealthChecks as ports are empty.");
            return manifestSnippetList;
        }
        // TODO supporting single health check
        V1Probe v1Probe = getHealthProbe(portsList);
        // TODO set successful threshold

        if (v1Probe != null) {
            v1Probe.setInitialDelaySeconds(DEFAULT_INITIAL_DELAY_IN_SECONDS);
            v1Probe.setPeriodSeconds(DEFAULT_PERIOD_IN_SECONDS);
            v1Probe.setTimeoutSeconds(DEFAULT_TIMEOUT_IN_SECONDS);
            v1Probe.setFailureThreshold(DEFAULT_FAILURE_THRESHOLD_IN_SECONDS);
            try {
                manifestSnippetList.add(buildReadinessProbe(v1Probe, podSpecOwner));
                manifestSnippetList.add(buildLiveinessProbe(v1Probe, podSpecOwner));
                logger.debug("Processing HealthChecks done.");
            } catch (JsonProcessingException e) {
                logger.error("Error while serializing health checks ", e);
            }

        }
        return manifestSnippetList;
    }

    private static V1Probe getHealthProbe(List<Port> portsList) {
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

    private static ManifestSnippet buildReadinessProbe(V1Probe v1Probe, String podSpecOwner) throws JsonProcessingException {
        if (v1Probe == null) {
            return null;
        }
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(v1Probe));
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.containers[0].readinessProbe");
        return manifestSnippet;
    }

    private static ManifestSnippet buildLiveinessProbe(V1Probe v1Probe, String podSpecOwner) throws JsonProcessingException {
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
