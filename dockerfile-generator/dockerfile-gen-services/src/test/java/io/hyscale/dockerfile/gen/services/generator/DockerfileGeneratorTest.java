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
package io.hyscale.dockerfile.gen.services.generator;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleError;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.dockerfile.gen.services.exception.DockerfileErrorCodes;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.services.util.ServiceSpecTestUtil;

@SpringBootTest
public class DockerfileGeneratorTest {

    @Autowired
    private DockerfileGenerator dockerfileGenerator;

    public static Stream<Arguments> skipGenInput() {
        return Stream.of(Arguments.of("/input/skip-generation/stack-as-service.hspec"),
                Arguments.of("/input/skip-generation/dockerfile.hspec"));
    }

    @ParameterizedTest
    @MethodSource("skipGenInput")
    public void skipDockerfileGenTest(String serviceSpecPath) {
        try {
            assertNull(dockerfileGenerator.generateDockerfile(ServiceSpecTestUtil.getServiceSpec(serviceSpecPath),
                    new DockerfileGenContext()));
        } catch (HyscaleException | IOException e) {
            fail(e);
        }
    }

    public static Stream<Arguments> invalidInput() {
        return Stream.of(Arguments.of("", CommonErrorCode.SERVICE_SPEC_REQUIRED),
                Arguments.of("/input/invalid-input/invalid-stack-image.hspec",
                        DockerfileErrorCodes.INVALID_STACK_IMAGE),
                Arguments.of("/input/invalid-input/myservice.hspec",
                        DockerfileErrorCodes.DOCKERFILE_OR_BUILDSPEC_REQUIRED));
    }

    @ParameterizedTest
    @MethodSource("invalidInput")
    public void invalidInputTest(String serviceSpecPath, HyscaleError hyscaleError) {
        try {
            dockerfileGenerator.generateDockerfile(ServiceSpecTestUtil.getServiceSpec(serviceSpecPath),
                    new DockerfileGenContext());
            fail();
        } catch (IOException e) {
            fail(e);
        } catch (HyscaleException ex) {
            if (ex.getHyscaleError() != hyscaleError) {
                fail(ex);
            }
        }
    }
}
