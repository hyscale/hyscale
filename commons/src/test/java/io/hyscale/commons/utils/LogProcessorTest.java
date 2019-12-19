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

import io.hyscale.commons.constants.TestConstants;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.handler.TailLogTestHandler;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.junit.jupiter.api.Nested;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class LogProcessorTest {
    private static final Logger logger = LoggerFactory.getLogger(LogProcessorTest.class);
    private static final String LOGS_DIRECTORY = "logs";
    private static final String LOG_FILE = "logs.txt";
    private static final String LOG_FILE_PATH = TestConstants.TMP_DIR + ToolConstants.FILE_SEPARATOR + LOGS_DIRECTORY + ToolConstants.FILE_SEPARATOR + LOG_FILE;
    private static final LogProcessor logProcessor = new LogProcessor();
    private static final String ENCODING = "UTF-8";
    private static final String logFileContent = "logger running" + "\n" + "logger running";
    private static File file;
    private static ByteArrayOutputStream os = new ByteArrayOutputStream();

    @BeforeEach
    public void getLogFile() {
        file = FileUtils.getFile(LOG_FILE_PATH);
        new File(file.getParent()).mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            logger.error("unable to create log file:" + LOG_FILE_PATH + " in the class " + this.getClass().toString());
        }
    }

    public static Stream<Arguments> getNullInputsForWrite() {
        InputStream inputStream = getInputStream();
        return Stream.of(Arguments.of(inputStream, null, CommonErrorCode.LOGFILE_NOT_FOUND),
                Arguments.of(null, LOG_FILE_PATH, CommonErrorCode.INPUTSTREAM_NOT_FOUND),
                Arguments.of(inputStream, "", CommonErrorCode.LOGFILE_NOT_FOUND));
    }

    public static Stream<Arguments> getNullInputsForRead() {
        return Stream.of(Arguments.of(null, os, null, CommonErrorCode.FAILED_TO_READ_LOGFILE),
                Arguments.of(new File(LOG_FILE), os, null, CommonErrorCode.FAILED_TO_READ_LOGFILE),
                Arguments.of(file.getParentFile(), os, null, CommonErrorCode.FAILED_TO_READ_LOGFILE),
                Arguments.of(file, null, null, CommonErrorCode.OUTPUTSTREAM_NOT_FOUND));
    }

    @Nested
    @DisplayName("Writing log file test cases.")
    public class WriteLogFileTests {

        @ParameterizedTest
        @MethodSource("io.hyscale.commons.utils.LogProcessorTest#getNullInputsForWrite")
        public void testNullConditionsForWrite(InputStream inputStream, String logFilePath, CommonErrorCode expectedErrorCode) {
            try {
                logProcessor.writeLogFile(inputStream, logFilePath);
            } catch (IOException e) {
                fail(e.getMessage());
            } catch (HyscaleException e) {
                assertEquals(e.getHyscaleErrorCode(), expectedErrorCode);
            }
        }

        @Test
        public void testWriteLogFile() {
            String content = null;
            try (InputStream inputStream = getInputStream()) {
                logProcessor.writeLogFile(inputStream, LOG_FILE_PATH);
                content = FileUtils.readFileToString(file, ENCODING).trim();
            } catch (IOException | HyscaleException i) {
                fail(i.getMessage());
            }
            assertNotNull(content);
            assertEquals(content, logFileContent);
        }
    }

    @Nested
    @DisplayName("Reading log file test cases.")
    public class ReadLogFileTests {

        @ParameterizedTest
        @MethodSource("io.hyscale.commons.utils.LogProcessorTest#getNullInputsForRead")
        public void testNullConditionsForRead(File readFile, OutputStream os, Integer lines, CommonErrorCode expectedErrorCode) {
            try {
                logProcessor.readLogFile(readFile, os, lines);
            } catch (HyscaleException e) {
                assertEquals(expectedErrorCode, e.getHyscaleErrorCode());
            }
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(ints = 1)
        public void readLogFileTest(Integer lines) {
            os.reset();
            System.out.println(os.toString());
            try {
                assertTrue(file.exists());
                FileUtils.writeStringToFile(file, logFileContent, ENCODING);
                logProcessor.readLogFile(file, os, lines);
            } catch (IOException | HyscaleException i) {
                fail(i.getMessage());
            }
            assertNotNull(os);
            assertEquals(os.toString().trim(), logFileContent);
        }
    }

    @Nested
    @DisplayName("Tailing log file test cases.")
    public class TailLogFileTests {
        private TailLogTestHandler tailLogTestHandler = new TailLogTestHandler();

        @Test
        public void testTailLogFile() {
            long currentTimeInMillis = System.currentTimeMillis();
            long timeLimit = currentTimeInMillis + 2000;
            List<String> lines = Arrays.asList("logger running", "logger running", "logger running", "logger running", "EXIT");
            TailLogFile tailLogFile = null;
            tailLogFile = logProcessor.tailLogFile(file, tailLogTestHandler);
            Thread fileWriterThread = new Thread(() -> {
                for (String line : lines) {
                    try {
                        FileUtils.writeStringToFile(file, line + "\n", ENCODING, true);
                        Thread.sleep(100);
                    } catch (IOException | InterruptedException e) {
                        logger.error(e.getMessage());
                    }

                }
            });
            fileWriterThread.start();

            while (tailLogFile.isRunning()) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }
                if (System.currentTimeMillis() >= timeLimit) {
                    tailLogFile.stopRunning();
                    fail("Tail timed out ,End of file did not match.");
                }
            }

            List<String> loggedLines = tailLogTestHandler.getLines();
            assertNotNull(loggedLines);
            assertEquals(loggedLines, lines);
        }

        @Test
        public void testNullConditionsForTail() {
            assertNull(logProcessor.tailLogFile(null, tailLogTestHandler));
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

