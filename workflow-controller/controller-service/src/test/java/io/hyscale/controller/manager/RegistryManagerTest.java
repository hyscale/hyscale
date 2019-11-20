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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    @Test
    public void testRegistryDetails() {

        ImageRegistry registry = null;
        String registryUrl = "test.registry.com";
        try {
            registry = registryManager.getImageRegistry(registryUrl);
        } catch (HyscaleException e) {
            fail();
        }

        assertNotNull(registry);
        assertEquals(registryUrl, registry.getUrl());
        String userName = "admin";
        String password = "admin";
        assertEquals(userName, registry.getUserName());
        assertEquals(password, registry.getPassword());
    }

    @Test
    public void testRegistryNotFound() {

        ImageRegistry registry = null;
        String registryUrl = "doesNotExist";
        try {
            registry = registryManager.getImageRegistry(registryUrl);
        } catch (HyscaleException e) {
        }
        assertNull(registry);
    }

    @Test
    public void testNullRegistry() {
        try {
            assertNull(registryManager.getImageRegistry(null));
        } catch (HyscaleException e) {
        }
    }
}
