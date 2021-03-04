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
package io.hyscale.commons.io;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.handler.TailLogTestHandler;
import io.hyscale.commons.io.LogProcessor;
import io.hyscale.commons.io.TailLogFile;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.junit.jupiter.api.Nested;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogProcessorTests {
    private static final String LOG_FILE_PATH = "/tmp/logs/logs.txt";
    private static final LogProcessor logProcessor = new LogProcessor();
    private static final String ENCODING = "UTF-8";
    private static final String logFileContent = "logger running" + System.lineSeparator() + "logger running";
    private static File file;
    private static ByteArrayOutputStream os = new ByteArrayOutputStream();

    @BeforeEach
    public void getLogFile() {
        file = FileUtils.getFile(LOG_FILE_PATH);
        new File(file.getParent()).mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            Assertions.fail("unable to create log file:" + LOG_FILE_PATH + " in the class " + this.getClass().toString());
        }
    }

    public static Stream<Arguments> getNullInputsForWrite() {
        InputStream inputStream = getInputStream();
        return Stream.of(Arguments.of(inputStream, null),
                Arguments.of(null, LOG_FILE_PATH),
                Arguments.of(inputStream, ""));
    }

    public static Stream<Arguments> getNullInputsForRead() {
        return Stream.of(Arguments.of(null, os, null),
                Arguments.of(file, null, null));
    }

    @Nested
    @DisplayName("Writing log file test cases.")
    public class WriteLogFileTests {

        @ParameterizedTest
        @MethodSource("io.hyscale.commons.io.LogProcessorTests#getNullInputsForWrite")
        public void testNullConditionsForWrite(InputStream inputStream, String logFilePath) {
            Assertions.assertThrows(HyscaleException.class, () -> {
                logProcessor.writeLogFile(inputStream, logFilePath);
            });
        }

        @Test
        public void testWriteLogFile() {
            String content = null;
            try (InputStream inputStream = getInputStream()) {
                logProcessor.writeLogFile(inputStream, LOG_FILE_PATH);
                content = FileUtils.readFileToString(file, ENCODING).trim();
            } catch (IOException i) {
                Assertions.fail();
            } catch (HyscaleException e) {
                Assertions.fail();
            }
            Assertions.assertNotNull(content);
            Assertions.assertEquals(logFileContent, content );
        }
    }

    @Nested
    @DisplayName("Reading log file test cases.")
    public class ReadLogFileTests {

        @ParameterizedTest
        @MethodSource("io.hyscale.commons.io.LogProcessorTests#getNullInputsForRead")
        public void testNullConditionsForRead(File readFile, OutputStream os, Integer lines) {
            if (readFile == null || !readFile.isDirectory() || !readFile.exists() || os == null) {
                Assertions.assertThrows(HyscaleException.class, () -> {
                    logProcessor.readLogFile(readFile, os, lines);
                });
            }
        }

        @ParameterizedTest
        @NullSource
        @CsvSource({"1"})
        public void readLogFileTest(Integer lines) {
            os.reset();
            System.out.println(os.toString());
            try {
                Assertions.assertTrue(file.exists());
                FileUtils.writeStringToFile(file, logFileContent, ENCODING);
                logProcessor.readLogFile(file, os, lines);
            } catch (IOException i) {
                Assertions.fail(i.getMessage());
            } catch (HyscaleException e) {
            }
            Assertions.assertNotNull(os);
            Assertions.assertEquals(logFileContent, os.toString().trim());
        }
    }

    @Nested
    @DisplayName("Tailing log file test cases.")
    @SuppressWarnings("java:S2925")
    public class TailLogFileTests {
        private TailLogTestHandler tailLogTestHandler = new TailLogTestHandler();

        @Test
        public void testTailLogFile() {
            long currentTimeInMillis = System.currentTimeMillis();
            long timeLimit = currentTimeInMillis + 2000;
            List<String> lines = Stream.of("logger running", "logger running", "logger running", "logger running", "EXIT").collect(Collectors.toList());
            TailLogFile tailLogFile = null;
            tailLogFile = logProcessor.tailLogFile(file, tailLogTestHandler);
            Thread fileWriterThread = new Thread(() -> {
                for (String line : lines) {
                    try {
                        FileUtils.writeStringToFile(file, line + "\n", ENCODING, true);
                        Thread.sleep(100);
                    } catch (IOException | InterruptedException e) {
                    }

                }
            });
            fileWriterThread.start();

            while (tailLogFile.isRunning()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
                if (System.currentTimeMillis() >= timeLimit) {
                    tailLogFile.stopRunning();
                    Assertions.fail("Tail timed out ,End of file did not match.");
                }
            }

            List<String> loggedLines = tailLogTestHandler.getLines();
            Assertions.assertNotNull(loggedLines);
            Assertions.assertEquals(loggedLines, lines);
        }

        @Test
        public void testNullConditionsForTail() {
            Assertions.assertNull(logProcessor.tailLogFile(null, tailLogTestHandler));
        }
    }

    private static InputStream getInputStream() {
        InputStream inputStream = new ByteArrayInputStream(logFileContent.getBytes(Charset.forName(ENCODING)));
        return inputStream;
    }

    @AfterEach
    public void deleteLogFile() {
        if (file.exists()) {
            file.delete();
        }
    }
}

