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
package io.hyscale.dockerfile.gen.services.processor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.services.processor.impl.BuildSpecValidatorProcessor;
import io.hyscale.dockerfile.gen.services.util.ServiceSpecTestUtil;

public class BuildSpecValidatorTest {

    private BuildSpecValidatorProcessor buildSpecValidatorProcessor = new BuildSpecValidatorProcessor();

    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of(null, HyscaleException.class),
                Arguments.of("/servicespecs/invalid_buildSpec1.hspec", HyscaleException.class),
                Arguments.of("/servicespecs/invalid_buildSpec2.hspec", HyscaleException.class));
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    public void testInvalidServiceSpec(String serviceSpecPath, Class exception) {
        assertThrows(exception, () -> {
            buildSpecValidatorProcessor.preProcess(ServiceSpecTestUtil.getServiceSpec(serviceSpecPath),
                    new DockerfileGenContext());
        });
    }

    @Test
    public void validBuildSpec() {
        try {
            buildSpecValidatorProcessor.preProcess(
                    ServiceSpecTestUtil.getServiceSpec("/servicespecs/myservice.hspec"),
                    new DockerfileGenContext());
        } catch (IOException | HyscaleException e) {
            fail();
        }
    }

}
