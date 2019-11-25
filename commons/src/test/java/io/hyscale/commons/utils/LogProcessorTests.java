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

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.handler.SampleLogHandler;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogProcessorTests {
    private static String logFileContent = "logger running" + "\n" + "logger Running";
    private LogProcessor logProcessor = new LogProcessor();
    private static String logFilePath = "/tmp/logs/logs.txt";
    private static String encoding = "UTF-8";
    private static File file;
    private SampleLogHandler sampleLogHandler = new SampleLogHandler();


    @BeforeEach
    public void getLogFile() {
        file = FileUtils.getFile(logFilePath);
        try {
            file.createNewFile();
        } catch (IOException e) {
        }
    }

    public static Stream<Arguments> getNullInputsForWrite() {
        InputStream inputStream = getInputStream();
        return Stream.of(Arguments.of(inputStream, null),
                Arguments.of(null, logFilePath),
                Arguments.of(inputStream, ""));
    }

    @ParameterizedTest
    @MethodSource(value = "getNullInputsForWrite")
    public void testNullConditionsForWrite(InputStream inputStream, String logFilePath) {
        Assertions.assertThrows(HyscaleException.class, () -> {
            logProcessor.writeLogFile(inputStream, logFilePath);
        });
    }

    @Test
    public void testWriteLogFile() {
        String content = null;
        InputStream inputStream = getInputStream();
        try {
            logProcessor.writeLogFile(inputStream, logFilePath);
            content = FileUtils.readFileToString(file, encoding).trim();
            inputStream.close();
        } catch (IOException i) {
            Assertions.fail();
        } catch (HyscaleException e) {
        }
        Assertions.assertNotNull(content);
        Assertions.assertEquals(content, logFileContent);
    }

    public static Stream<Arguments> getNullInputsForRead() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        return Stream.of(Arguments.of(null, os, null),
                Arguments.of(file, null, null),
                Arguments.of(file, os, 1),
                Arguments.of(file, os, null));
    }

    @ParameterizedTest
    @MethodSource(value = "getNullInputsForRead")
    public void readLogFileTest(File readFile, OutputStream os, Integer lines) {
        if (readFile == null || !readFile.isDirectory() || !readFile.exists()) {
        } else if (os == null) {
            Assertions.assertThrows(HyscaleException.class, () -> {
                logProcessor.readLogFile(readFile, os, lines);
            });
        } else {
            System.out.println("came " + readFile.getPath());
            if (lines != null) {
                String[] logLines = logFileContent.split("\\r?\\n");
                logFileContent = logLines[0];
            }
            try {
                Assertions.assertTrue(readFile.exists());
                FileUtils.writeStringToFile(readFile, logFileContent, encoding);
                logProcessor.readLogFile(readFile, os, lines);
            } catch (IOException i) {
                Assertions.fail(i.getMessage());
            } catch (HyscaleException e) {
            }
            Assertions.assertNotNull(os);
            Assertions.assertEquals(os.toString().trim(), logFileContent);
        }
    }


    @Test
    public void testTailLogFile() {
        long currentTimeInMillis = System.currentTimeMillis();
        long timeLimit = currentTimeInMillis + 2000;
        List<String> lines = Stream.of("logger running", "logger running", "logger running", "logger running", "EXIT").collect(Collectors.toList());
        TailLogFile tailLogFile = null;
        try {
            tailLogFile = logProcessor.tailLogFile(file, sampleLogHandler);
        } catch (HyscaleException e) {
        }

        Thread fileWriterThread = new Thread(() -> {
            for (String line : lines) {
                try {
                    FileUtils.writeStringToFile(file, line + "\n", encoding, true);
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

        List<String> loggedLines = sampleLogHandler.getLines();
        Assertions.assertNotNull(loggedLines);
        Assertions.assertEquals(loggedLines, lines);
    }

    @Test
    public void testNullConditionsForTail() {
        Assertions.assertThrows(HyscaleException.class, () -> {
            logProcessor.tailLogFile(file, null);
        });

        try {
            Assertions.assertNull(logProcessor.tailLogFile(null, sampleLogHandler));
        } catch (HyscaleException e) {
            Assertions.fail(e.getMessage());
        }

    }

    private static InputStream getInputStream() {
        InputStream inputStream = new ByteArrayInputStream(logFileContent.getBytes(Charset.forName(encoding)));
        return inputStream;
    }

    @AfterEach
    public void deleteLogFile() {
        if (file.exists()) {
            file.delete();
        }
    }
}
