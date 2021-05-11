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

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.K8sServiceType;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringBootTest
class ServiceTypeTest {

    @Autowired
    private ServiceTypeHandler serviceTypeHandler;

    private static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice-min.hspec"), null),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice.hspec"),
                        K8sServiceType.CLUSTER_IP.getName()),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/plugins/service/internal.hspec"),
                        K8sServiceType.CLUSTER_IP.getName()),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/input/myservice-2.hspec"),
                        K8sServiceType.LOAD_BALANCER.getName()));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testServiceTypeHandler(ServiceSpec serviceSpec, String expectedServiceType)
            throws HyscaleException, IOException {
        List<ManifestSnippet> manifestList = serviceTypeHandler.handle(serviceSpec, null);

        if (StringUtils.isBlank(expectedServiceType)) {
            assertTrue(CollectionUtils.isEmpty(manifestList));
            return;
        }

        assertTrue(manifestList.stream().allMatch(manifest -> manifest.getPath().equals("spec.type")
                && manifest.getSnippet().equals(expectedServiceType)));
    }
}
