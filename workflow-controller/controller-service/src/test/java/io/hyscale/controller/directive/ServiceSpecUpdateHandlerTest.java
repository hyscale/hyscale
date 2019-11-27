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
package io.hyscale.controller.directive;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.controller.util.ServiceSpecTestUtil;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ServiceSpecUpdateHandlerTest {

    private static ObjectNode serviceSpecNode = null;

    private static ServiceSpec oldServiceSpec = null;

    private static ServiceSpec updatedServiceSpec = null;

    @Autowired
    private PropsUpdateTestHandler propsJsonHandlerTest;

    @Autowired
    private DockerfileUpdateTestHandler dockerfileJsonHandlerTest;

    @Autowired
    private BuildSpecUpdateTestHandler buildSpecJsonHandlerTest;

    public Stream<ServiceSpecUpdateTestHandler> input() {
        return Stream.of(propsJsonHandlerTest, dockerfileJsonHandlerTest, buildSpecJsonHandlerTest);
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    public void testServiceSpecUpdate(ServiceSpecUpdateTestHandler serviceSpecJsonHandlerTest) {
        String serviceSpec = serviceSpecJsonHandlerTest.getServiceSpec();
        try {
            oldServiceSpec = ServiceSpecTestUtil.getServiceSpec(serviceSpec);
            serviceSpecNode = (ObjectNode) ServiceSpecTestUtil.getServiceSpecJsonNode(serviceSpec);
            updatedServiceSpec = serviceSpecJsonHandlerTest.getUpdatedServiceSpec(serviceSpecNode);
            serviceSpecJsonHandlerTest.performValidation(oldServiceSpec, updatedServiceSpec);
        } catch (IOException e) {
            fail();
        }
    }

}
