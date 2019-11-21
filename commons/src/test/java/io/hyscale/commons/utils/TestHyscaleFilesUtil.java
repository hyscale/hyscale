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

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URL;

public class TestHyscaleFilesUtil {
    private static URL SAMPLE_FILE_URL;
    private static String SAMPLE_FILE;
    private static File sampleFile;
    private static String testDirPath;
    private static String testFilePath;
    private static File testDir;
    private static String sampleFileName = "sample.txt";

    @BeforeAll
    public static void init() {
        SAMPLE_FILE_URL = TestHyscaleFilesUtil.class.getClassLoader().getResource(sampleFileName);
        SAMPLE_FILE = SAMPLE_FILE_URL.getPath();
        testDirPath = ToolConstants.TMP_DIR + ToolConstants.FILE_SEPARATOR + "testDir";
        testFilePath = testDirPath + ToolConstants.FILE_SEPARATOR + "testFile";
        sampleFile = FileUtils.getFile(SAMPLE_FILE);
        new File(testDirPath).mkdirs();
        testDir = new File(testDirPath);
    }


    @Test
    public void createFileTest() throws HyscaleException {
        StringBuilder expected = new StringBuilder();
        expected.append(testFilePath);
        File file = HyscaleFilesUtil.createFile(expected.toString(), "this is some file content");
        assertTrue(file.exists());
    }


    @Test
    public void updateFileTest() throws HyscaleException {
        File file = FileUtils.getFile(testFilePath);
        long l1 = file.length();
        if(file.exists()) {
            long l2 = HyscaleFilesUtil.updateFile(testFilePath, "updated.").length();
            assertTrue(l1 < l2 ? true : false);
        }
    }

    @Test
    public void copyFileToDirTest() throws HyscaleException {
        HyscaleFilesUtil.copyFileToDir(sampleFile, testDir);
        File file = FileUtils.getFile(testDir + ToolConstants.FILE_SEPARATOR + HyscaleFilesUtil.getFileName(SAMPLE_FILE));
        assertNotNull(file);
        assertTrue(file.exists());
    }

    @Test
    public void copyFileTest() throws HyscaleException {
        File testFile = FileUtils.getFile(testFilePath);
        HyscaleFilesUtil.copyFile(sampleFile, testFile);
        assertTrue(testFile.length() == sampleFile.length() ? true : false);
    }

    @Test
    public void getFileNameTest() throws HyscaleException {
        assertEquals(sampleFileName, HyscaleFilesUtil.getFileName(SAMPLE_FILE));
    }

    @Test
    public void clearDirectoryTest() throws Exception {
        File testFile = FileUtils.getFile(testFilePath);
        assertNotNull(testFile);
        HyscaleFilesUtil.clearDirectory(testDirPath);
        assertFalse(testFile.exists());

    }

    @Test
    public void deleteDirectoryTest() throws HyscaleException {
        HyscaleFilesUtil.deleteDirectory(testDirPath);
        assertFalse(testDir.exists());

    }

    @AfterAll
    public static void cleanUp() throws Exception {
        if (testDir.exists()) {
            FileUtils.deleteDirectory(testDir);
        }
    }
}
