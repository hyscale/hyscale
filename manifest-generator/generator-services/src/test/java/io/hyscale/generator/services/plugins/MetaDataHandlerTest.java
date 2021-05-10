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
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MetaDataHandlerTest {

    private ServiceSpec serviceSpec;

    @Autowired
    private MetaDataHandler metaDataHandler;

    @BeforeAll
    public void init() throws HyscaleException {
        serviceSpec = ServiceSpecTestUtil.getServiceSpec("/input/myservice.hspec");
    }

    private Stream<Arguments> input() {
        return Stream.of(
                Arguments.of(serviceSpec, ManifestContextTestUtil.getManifestContext(ManifestResource.DEPLOYMENT)));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testHandle(ServiceSpec serviceSpec, ManifestContext manifestContext) {
        try {
            List<ManifestSnippet> manifestList = metaDataHandler.handle(serviceSpec, manifestContext);
            Assertions.assertTrue(manifestList != null && !manifestList.isEmpty());
        } catch (HyscaleException e) {
            fail(e);
        }
    }
}