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
package io.hyscale.builder.services.handler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBuildLogHandler {
    private BuildLogHandler buildLogHandler;
    private static String EOF_MARKER = "(.*)Successfully tagged(.*)";
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public  void performBeforeAll() {
        buildLogHandler = new BuildLogHandler();;
        System.setOut(new PrintStream(outputStream));
    }

    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of(null,true),
                Arguments.of(EOF_MARKER,true));
    }

    @DisplayName("Performing funtionality test on HandleEOF: Expected True")
    @ParameterizedTest
    @MethodSource(value = "input")
    public void testHandleEOF(String line,Boolean flag) {
        assertEquals(buildLogHandler.handleEOF(line),flag);
    }

    @Test
    public  void testHandle(){
        buildLogHandler.handleLine("test");
        assertEquals("test\n", outputStream.toString());
    }

    @AfterEach
    public  void perforemAfterAll() throws IOException {
        System.setOut(originalOut);
        outputStream.close();
    }
}
