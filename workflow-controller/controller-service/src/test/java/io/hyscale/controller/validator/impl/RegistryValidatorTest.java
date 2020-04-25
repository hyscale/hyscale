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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.stream.Stream;

import io.hyscale.controller.model.WorkflowContextBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringBootTest
public class RegistryValidatorTest {

    @Autowired
    private RegistryValidator registryValidator;

    @MockBean
    private RegistryManager registryManager;

    private static ServiceSpec validServiceSpec = null;

    private static ServiceSpec invalidServiceSpec = null;

    @BeforeAll
    public static void setUp() throws IOException {
        validServiceSpec = ServiceSpecTestUtil.getServiceSpec("/servicespecs/validator/registry_validation.hspec");
        invalidServiceSpec = ServiceSpecTestUtil.getServiceSpec("/servicespecs/validator/invalid-registry.hspec");

    }

    @BeforeEach
    public void initMocks() throws HyscaleException {
        Mockito.when(registryManager
                .getImageRegistry(validServiceSpec.get(HyscaleSpecFields.image, Image.class).getRegistry()))
                .thenReturn(new ImageRegistry());

        Mockito.when(registryManager
                .getImageRegistry(invalidServiceSpec.get(HyscaleSpecFields.image, Image.class).getRegistry()))
                .thenReturn(null);
    }

    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of(validServiceSpec, true), Arguments.of(invalidServiceSpec, false));
    }

    @ParameterizedTest
    @MethodSource("input")
    public void validateRegistry(ServiceSpec serviceSpec, boolean expectedResult) {
        try {
            WorkflowContext context = new WorkflowContextBuilder(null).withService(serviceSpec).get();
            assertEquals(expectedResult, registryValidator.validate(context));
        } catch (HyscaleException ex) {
            fail(ex);
        }
    }

}
