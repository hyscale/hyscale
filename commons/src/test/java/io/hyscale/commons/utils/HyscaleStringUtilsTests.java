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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HyscaleStringUtilsTests {
    private static final String SAMPLE_STRING = "hyscaleUser";
    private static final String SUFFIX_STRING = "User";
    private static final String EXPECTED_STRING = "hyscale";
    private static final char TAILING_CHAR = 'r';
    private static final String EXPECTED_CHAR_STRING = "hyscaleUse";

    public static Stream<Arguments> getInputsForRemoveSuffixTest() {
        return Stream.of(Arguments.of(null, SUFFIX_STRING, null),
                Arguments.of("", SUFFIX_STRING, ""),
                Arguments.of(SAMPLE_STRING, null, SAMPLE_STRING),
                Arguments.of(SAMPLE_STRING, "", SAMPLE_STRING),
                Arguments.of(SAMPLE_STRING, SUFFIX_STRING, EXPECTED_STRING));
    }

    @ParameterizedTest
    @MethodSource(value = "getInputsForRemoveSuffixTest")
    public void testRemoveSuffixStr(String sampleString, String suffixString, String expected) {
        String actualString = HyscaleStringUtil.removeSuffixStr(sampleString, suffixString);
        assertEquals(expected, actualString);
    }

    @ParameterizedTest
    @MethodSource(value = "getInputsForRemoveSuffixTest")
    public void testRemoveSuffixStrBuilder(String sampleString, String suffix, String expected) {
        StringBuilder strBuilder = getStringBuilderFor(sampleString);
        String actualString = HyscaleStringUtil.removeSuffixStr(strBuilder, suffix);
        assertEquals(expected, actualString);
    }

    public static Stream<Arguments> getSuffixCharInputs() {
        return Stream.of(Arguments.of(null, TAILING_CHAR, null),
                Arguments.of("", TAILING_CHAR, ""),
                Arguments.of(SAMPLE_STRING, TAILING_CHAR, EXPECTED_CHAR_STRING));
    }

    @ParameterizedTest
    @MethodSource(value = "getSuffixCharInputs")
    public void testRemoveSuffixChar(String sampleString, char trailingChar, String expected) {
        StringBuilder stringBuilder = getStringBuilderFor(sampleString);
        String actualString = HyscaleStringUtil.removeSuffixChar(stringBuilder, trailingChar);
        assertEquals(expected, actualString);
    }


    public StringBuilder getStringBuilderFor(String string) {
        if (string == null) {
            return null;
        }
        return new StringBuilder(string);
    }
}
