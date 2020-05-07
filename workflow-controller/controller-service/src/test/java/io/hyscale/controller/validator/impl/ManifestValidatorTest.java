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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.ServiceSpecTestUtil;

@SpringBootTest
public class ManifestValidatorTest {

    @Autowired
    private ManifestValidator manifestValidator;

    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of(null, false),
                Arguments.of("/servicespecs/invalid_vol.hspec", false),
                Arguments.of("/servicespecs/myservice.hspec", true));
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    public void testManifestValidator(String serviceSpecPath, boolean expectedResult) {
        try {
            WorkflowContext context = new WorkflowContextBuilder(null).withService(ServiceSpecTestUtil.getServiceSpec(serviceSpecPath)).get();
            assertEquals(expectedResult, manifestValidator.validate(context));
        } catch (IOException | HyscaleException e) {
            fail(e);
        }
    }

}
