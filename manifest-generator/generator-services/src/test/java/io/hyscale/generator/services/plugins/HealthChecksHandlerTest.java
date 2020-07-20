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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.GsonSnippetConvertor;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.openapi.models.V1Probe;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class HealthChecksHandlerTest {

    @Autowired
    private HealthChecksHandler healthChecksHandler;

    private ManifestContext context = ManifestContextTestUtil.getManifestContext(ManifestResource.STATEFUL_SET);

    @ParameterizedTest
    @ValueSource(strings = { "/input/myservice-min.hspec", "/plugins/health/service-no-health.hspec" })
    void testNoHealthCheck(String serviceSpecPath) throws HyscaleException {
        ServiceSpec serviceSpec = ServiceSpecTestUtil.getServiceSpec(serviceSpecPath);
        try {
            List<ManifestSnippet> manifests = healthChecksHandler.handle(serviceSpec, context);
            if (manifests != null && !manifests.isEmpty()) {
                fail("Health check manifest was not expected");
            }
        } catch (HyscaleException e) {
            fail(e);
        }
    }

    private Stream<Arguments> input() {
        return Stream.of(Arguments.of("/input/myservice.hspec", HealthCheckType.HTTPS),
                Arguments.of("/plugins/health/healthcheck-tcp.hspec", HealthCheckType.TCP));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testHealthCheck(String serviceSpecPath, HealthCheckType type) throws HyscaleException {
        ServiceSpec serviceSpec = ServiceSpecTestUtil.getServiceSpec(serviceSpecPath);
        List<ManifestSnippet> manifests = null;
        try {
            manifests = healthChecksHandler.handle(serviceSpec, context);
        } catch (HyscaleException e) {
            fail(e);
        }
        if (manifests == null || manifests.isEmpty()) {
            fail("Health check manifest expected");
        }
        assertTrue(manifests.stream().allMatch(manifest -> {
            V1Probe v1Probe;
            try {
                v1Probe = GsonSnippetConvertor.deserialize(manifest.getSnippet(), V1Probe.class);
                switch (type) {
                case HTTPS:
                    return v1Probe.getHttpGet() != null;
                case TCP:
                    return v1Probe.getTcpSocket() != null;

                }
            } catch (IOException e) {
                fail(e);
            }
            return false;
        }));
    }

    enum HealthCheckType {
        HTTPS, TCP
    }
}
