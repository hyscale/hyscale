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
package io.hyscale.controller.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ImageRegistry;
import io.hyscale.controller.config.ControllerConfig;
import io.hyscale.controller.manager.impl.LocalRegistryManagerImpl;

public class RegistryManagerTest {

    @Mock
    private ControllerConfig controllerConfig;

    @InjectMocks
    private LocalRegistryManagerImpl registryManager;

    private static String ABS_CONFIG_PATH;

    @BeforeAll
    public static void beforeTest() {
        ABS_CONFIG_PATH = RegistryManagerTest.class.getResource("/config/registry.json").getFile();
    }

    @BeforeEach
    public void initMocks() throws HyscaleException {
        MockitoAnnotations.initMocks(this);
        Mockito.when(controllerConfig.getDefaultRegistryConf()).thenReturn(ABS_CONFIG_PATH);
        // Calling post construct manually for mockito
        this.registryManager.init();
    }

    public static Stream<Arguments> input() {
        ImageRegistry registry = new ImageRegistry();
        registry.setUrl("test.my-test-registry.com");
        registry.setToken("dGVzdFVzZXI6dGVzdFBhc3N3b3Jk");
        return Stream.of(Arguments.of(null, null), Arguments.of("doesNotExist", null),
                Arguments.of("test.my-test-registry.com", registry));
    }

    @ParameterizedTest
    @MethodSource("input")
    public void testRegistry(String registryUrl, ImageRegistry expectedRegistry) {
        ImageRegistry registry = null;
        try {
            registry = registryManager.getImageRegistry(registryUrl);
        } catch (HyscaleException e) {
            fail();
        }
        assertEquals(expectedRegistry, registry);
    }

}
