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
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

@SpringBootTest
class NetworkPoliciesValidatorTest {

    @Autowired
    private NetworkPoliciesValidator networkPoliciesValidator;

    private static final Logger logger = LoggerFactory.getLogger(NetworkPoliciesValidatorTest.class);

    private static Stream<Arguments> input() throws HyscaleException {
        try {
            return Stream.of(
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-1.hspec"), true),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-2.hspec"), true),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-3.hspec"), true),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-4.hspec"), true),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-5.hspec"), true),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-6.hspec"), true),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-7.hspec"), false),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-8.hspec"), true),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-9.hspec"), false),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-10.hspec"), false),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-11.hspec"), false),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-12.hspec"), true),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-13.hspec"), true),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-14.hspec"), true),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-15.hspec"), false),
                    Arguments.of(ServiceSpecTestUtil.getServiceSpec("/network-policies/input/input-16.hspec"), true));
        } catch (Exception e) {
            HyscaleException ex = new HyscaleException(e, ManifestErrorCodes.ERROR_WHILE_CREATING_MANIFEST);
            logger.error("Error while generating Input Files", ex);
            throw ex;
        }
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    void testNetworkPoliciesValidator(ServiceSpec serviceSpec, boolean expectedOutput) throws HyscaleException {
            WorkflowContext context = new WorkflowContextBuilder(null).withService(serviceSpec).get();
            Assertions.assertEquals(expectedOutput, networkPoliciesValidator.validate(context));
    }
}
