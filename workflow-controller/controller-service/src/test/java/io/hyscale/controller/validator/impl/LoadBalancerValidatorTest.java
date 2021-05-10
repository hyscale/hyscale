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

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.model.WorkflowContextBuilder;
import io.hyscale.controller.util.ServiceSpecTestUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoadBalancerValidatorTest {

    @Autowired
    private LoadBalancerValidator loadBalancerValidator;


    public static Stream<Arguments> input() {
        return Stream.of(
                Arguments.of("/servicespecs/myservice.hspec", true),
                Arguments.of("/servicespecs/validator/lb-validator/lb-without-mandatory-fields.hspec", false),
                Arguments.of("/servicespecs/validator/lb-validator/lb-with-external-false.hspec", false),
                Arguments.of("/servicespecs/validator/lb-validator/lb-invalid-mapping.hspec", false),
                Arguments.of("/servicespecs/validator/lb-validator/lb-invalidPorts.hspec", false),
                Arguments.of("/servicespecs/validator/lb-validator/lb-istio-without-labels.hspec", false),
                Arguments.of("/servicespecs/validator/lb-validator/valid-lb-istio.hspec", true),
                Arguments.of("/servicespecs/validator/lb-validator/valid-lb-ingress.hspec", true));
    }


    @ParameterizedTest
    @MethodSource(value = "input")
    void testValidate(String serviceSpecPath, boolean expectedResult) {
        try {
            WorkflowContext context = new WorkflowContextBuilder("book-info").withService(ServiceSpecTestUtil.getServiceSpec(serviceSpecPath)).get();
            boolean validate = loadBalancerValidator.validate(context);
            assertEquals(expectedResult, validate);
        } catch (HyscaleException e) {
            fail(e);
        }
    }
}
