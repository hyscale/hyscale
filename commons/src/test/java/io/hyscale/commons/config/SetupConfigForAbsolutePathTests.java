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
package io.hyscale.commons.config;

import io.hyscale.commons.constants.ToolConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SetupConfigForAbsolutePathTests {
    private static String absolutePath;

    @BeforeAll
    public static void setAbsolutePath() {
        absolutePath = ToolConstants.FILE_SEPARATOR+"absolute"+ToolConstants.FILE_SEPARATOR+"path"+ToolConstants.FILE_SEPARATOR+"test";
        SetupConfig.setAbsolutePath(absolutePath);
    }


    public static Stream<Arguments> input() {
        URL resourceAsUrl = SetupConfigForAbsolutePathTests.class.getResource("/sample.txt");
        String sample_path = resourceAsUrl.getPath();
        File f = new File(sample_path);
        return Stream.of(Arguments.of("notBlank", absolutePath + ToolConstants.FILE_SEPARATOR + "notBlank"),
                Arguments.of(" ", SetupConfig.CURRENT_WORKING_DIR),
                Arguments.of(f.getAbsolutePath(), f.getAbsolutePath()));

    }

    @ParameterizedTest
    @MethodSource(value = "input")
    public void testGetAbsolutePath(String inputGiven, String expected) {
        String absPath = SetupConfig.getAbsolutePath(inputGiven);
        assertEquals(expected, absPath);
    }

    @AfterAll
    public static void unsetAbsolutePath() {
        SetupConfig.clearAbsolutePath();
    }
}
