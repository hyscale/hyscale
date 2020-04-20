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
package io.hyscale.controller.validator.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ImageRegistry;
import io.hyscale.controller.manager.RegistryManager;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.ServiceSpecTestUtil;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Image;

@SpringBootTest
public class RegistryValidatorTest {

    @Autowired
    private RegistryValidator registryValidator;

    @MockBean
    private RegistryManager registryManager;

    private static WorkflowContext context = new WorkflowContext();

    @BeforeAll
    public static void setUp() throws IOException {
        context.setServiceSpec(ServiceSpecTestUtil.getServiceSpec("/servicespecs/validator/registry_validation.hspec"));
    }

    @Test
    @Order(0)
    public void testInvalidCase() {
        try {
            Mockito.when(registryManager
                    .getImageRegistry(context.getServiceSpec().get(HyscaleSpecFields.image, Image.class).getRegistry()))
                    .thenReturn(null);
            assertFalse(registryValidator.validate(context));
        } catch (HyscaleException e) {
            fail(e);
        }
    }

    @Test
    public void testValidCase() {
        try {
            Mockito.when(registryManager
                    .getImageRegistry(context.getServiceSpec().get(HyscaleSpecFields.image, Image.class).getRegistry()))
                    .thenReturn(new ImageRegistry());
            assertTrue(registryValidator.validate(context));
        } catch (HyscaleException e) {
            fail(e);
        }
    }

}
