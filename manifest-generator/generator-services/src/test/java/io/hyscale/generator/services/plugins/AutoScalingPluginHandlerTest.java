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
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.utils.ManifestContextTestUtil;
import io.hyscale.generator.services.utils.ServiceSpecTestUtil;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class AutoScalingPluginHandlerTest {

    @Autowired
    private AutoScalingPluginHandler autoScalingPluginHandler;
  
    @MockBean
    private MustacheTemplateResolver mustacheTemplateResolver;

    private ServiceSpec noScalingSpec;

    private ServiceSpec scalingEnabledSpec;

    @BeforeAll
    public void init() throws HyscaleException {
        Mockito.when(mustacheTemplateResolver.resolveTemplate(anyString(), anyMap())).thenReturn("data");
        noScalingSpec = ServiceSpecTestUtil.getServiceSpec("/input/myservice-min.hspec");
        scalingEnabledSpec = ServiceSpecTestUtil.getServiceSpec("/input/myservice.hspec");
    }

    private Stream<Arguments> skipAutoScalingInput() {
        return Stream.of(Arguments.of(noScalingSpec, ManifestContextTestUtil.getManifestContext(null)),
                Arguments.of(noScalingSpec, ManifestContextTestUtil.getManifestContext(ManifestResource.STATEFUL_SET)),
                Arguments.of(noScalingSpec, ManifestContextTestUtil.getManifestContext(ManifestResource.DEPLOYMENT)),
                Arguments.of(scalingEnabledSpec, ManifestContextTestUtil.getManifestContext(null)));
    }

    @ParameterizedTest
    @MethodSource("skipAutoScalingInput")
    void skipAutoScaling(ServiceSpec serviceSpec, ManifestContext context) {
        try {
            List<ManifestSnippet> manifestList = autoScalingPluginHandler.handle(serviceSpec, context);
            if (manifestList != null && !manifestList.isEmpty()) {
                fail("HorizontalPodAutoscaler manifest found even when not required");
            }
        } catch (HyscaleException e) {
            fail(e);
        }
    }

    @Test
    void autoScalingEnabled() {
        try {
            List<ManifestSnippet> manifestList = autoScalingPluginHandler.handle(scalingEnabledSpec,
                    ManifestContextTestUtil.getManifestContext(ManifestResource.STATEFUL_SET));
            if (manifestList == null || manifestList.isEmpty()) {
                fail("HorizontalPodAutoscaler manifest required");
            }
        } catch (HyscaleException e) {
            fail(e);
        }
    }

}
