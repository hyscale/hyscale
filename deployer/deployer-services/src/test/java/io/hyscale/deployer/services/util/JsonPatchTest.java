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
package io.hyscale.deployer.services.util;

import io.hyscale.commons.exception.HyscaleException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

class JsonPatchTest {

    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of("/source.json", "/target.json",
                "[{\"op\":\"add\",\"path\":\"/spec/containers/0/ports/1\",\"value\":{\"name\":\"secure\",\"containerPort\":443,\"protocol\":\"TCP\"}},{\"op\":\"replace\",\"path\":\"/spec/containers/0/image\",\"value\":\"nginx\"}]")
        );
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    @Disabled
    void testJsonPatch(String source, String target, String expectedResult) throws HyscaleException, IOException {
        InputStream resourceAsStream = JsonPatchTest.class.getResourceAsStream(source);
        source = IOUtils.toString(resourceAsStream, "UTF-8");
        resourceAsStream.close();
        InputStream targetStream = JsonPatchTest.class.getResourceAsStream(target);
        target = IOUtils.toString(targetStream, "UTF-8");
        targetStream.close();
        Object patch = K8sResourcePatchUtil.getJsonPatch(source, target, String.class);
        Assertions.assertNotNull(patch);
    }
}
