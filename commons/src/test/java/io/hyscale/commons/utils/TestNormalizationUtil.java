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
package io.hyscale.commons.utils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestNormalizationUtil {
    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of("normaLize","normalize"),
                Arguments.of("normalize@1","normalize1"),
                Arguments.of(null,null),
                Arguments.arguments(" ",""),
                Arguments.arguments("normalize@ 1","normalize1"),
                Arguments.arguments("normalize@1 ","normalize1"),
                Arguments.arguments("normalize@1$","normalize1"),
                Arguments.arguments("norm@3alize ","norm3alize"));
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    public void testNormalizeWithLenth(String input,String expected) {
        String actualString = NormalizationUtil.normalize(input, 10);
        assertEquals(expected, actualString);
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    public void testNormalize(String input,String expected) {
        String actualString = NormalizationUtil.normalize(input);
        assertEquals(expected, actualString);
    }

}
