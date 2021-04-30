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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.hyscale.commons.models.Credentials;

class EncodeDecodeUtilTests {

    private static Stream<Arguments> encodeInput() {
        return Stream.of(Arguments.of(null, null, null),
                Arguments.of(null, "", null),
                Arguments.of("", null, null),
                Arguments.of("test", "test", Base64.getEncoder().encodeToString(("test" + ":" + "test").getBytes()))
                );
    }
    
    @ParameterizedTest
    @MethodSource("encodeInput")
    void testEncode(String userName, String password, String expectedResult) {
        assertEquals(expectedResult, EncodeDecodeUtil.getEncodedCredentials(userName, password));
    }
    
    private static Stream<Arguments> decodeInput() {
        return Stream.of(Arguments.of(null, null),
                Arguments.of("", ""),
                Arguments.of("dGVzdDp0ZXN0", "test:test")
                );
    }
    
    @ParameterizedTest
    @MethodSource("decodeInput")
    void testDecode(String input, String expectedResult) {
        assertEquals(expectedResult, EncodeDecodeUtil.decode(input));
    }
    
    private static Stream<Arguments> decodeCredentialsInput() {
        Credentials credentials = new Credentials();
        credentials.setUsername("test");
        credentials.setPassword("test");
        return Stream.of(Arguments.of(null, null),
                Arguments.of("", null),
                Arguments.of("dGVzdDp0ZXN0", credentials)
                );
    }
    
    @ParameterizedTest
    @MethodSource("decodeCredentialsInput")
    void testDecodedCredentials(String input, Credentials expectedCredentials) {
        Credentials actualCredentials = EncodeDecodeUtil.getDecodedCredentials(input);
        assertEquals(expectedCredentials, actualCredentials);
    }
}
