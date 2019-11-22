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
package io.hyscale.controller.hooks;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.ControllerTestInitializer;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.ServiceSpecTestUtil;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringJUnitConfig(classes = ControllerTestInitializer.class)
public class ManifestValidatorTest {

    @Autowired
    private ManifestValidatorHook manifestValidatorHook;

    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of(null, HyscaleException.class),
                Arguments.of("/servicespecs/invalid_vol.hspec.yaml", HyscaleException.class),
                Arguments.of("/servicespecs/invalid_ports.hspec.yaml", HyscaleException.class));
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    public void testInvalidManifest(String serviceSpecPath, Class klazz) {
        WorkflowContext context = new WorkflowContext();
        ServiceSpec serviceSpec = null;
        try {
            serviceSpec = ServiceSpecTestUtil.getServiceSpec(serviceSpecPath);
        } catch (IOException e1) {
            fail();
        }
        context.setServiceSpec(serviceSpec);
        assertThrows(klazz, () -> {
            manifestValidatorHook.preHook(context);
        });
    }

    @Test
    public void validManifest() {
        WorkflowContext context = new WorkflowContext();
        try {
            context.setServiceSpec(ServiceSpecTestUtil.getServiceSpec("/servicespecs/myservice.hspec.yaml"));
        } catch (IOException e) {
            fail();
        }
        try {
            manifestValidatorHook.preHook(context);
        } catch (HyscaleException e) {
            fail();
        }
    }

}
