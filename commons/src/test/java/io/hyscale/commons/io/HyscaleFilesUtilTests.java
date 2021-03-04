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

import io.hyscale.commons.constants.TestConstants;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class HyscaleFilesUtilTests {
    private static URL sampleFileUrl;
    private static String sampleFilePath;
    private static File sampleFile;
    private static String testDirPath;
    private static String testFilePath;
    private static File testDir;
    private static final String SAMPLE_FILE_NAME = "sample.txt";
    private static final String TEST_FILE_NAME = "test-file.txt";
    private static final String TEST_FILE_PATTERN = "^(test-).*(.txt)$";


    @BeforeAll
    public static void init() {
        sampleFileUrl = HyscaleFilesUtilTests.class.getClassLoader().getResource(SAMPLE_FILE_NAME);
        sampleFilePath = sampleFileUrl.getPath();
        testDirPath = TestConstants.TMP_DIR + ToolConstants.FILE_SEPARATOR + "testDir" + ToolConstants.FILE_SEPARATOR;
        testFilePath = testDirPath + TEST_FILE_NAME;
        sampleFile = FileUtils.getFile(sampleFilePath);
        testDir = new File(testDirPath);
    }

    private File createFile(String filePath) {
        File newFile = new File(filePath);
        createDirectory(newFile.getParent());
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            Assertions.fail("Unable to create file:" + filePath + " for " + getClass().toString());
        }
        return newFile;
    }

    private File createDirectory(String path) {
        new File(path).mkdirs();
        return new File(path);
    }

    private void deleteDirectory(File file) {
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
            }
        }
    }

    @Test
    public void createFileTest() throws HyscaleException {
        File file = HyscaleFilesUtil.createFile(testFilePath, "this is some file content");
        assertTrue(testDir.isDirectory());
        assertTrue(file.exists());
        deleteDirectory(testDir);
    }


    @Test
    public void updateFileTest() throws HyscaleException {
        File file = createFile(testFilePath);
        long l1 = file.length();
        if (file.exists()) {
            long l2 = HyscaleFilesUtil.updateFile(testFilePath, "updated.").length();
            assertTrue(l1 < l2);
        }
        deleteDirectory(testDir);
    }

    @Test
    public void copyFileToDirTest() throws HyscaleException {
        HyscaleFilesUtil.copyFileToDir(sampleFile, testDir);
        assertTrue(testDir.isDirectory());
        assertTrue(new File(testDirPath, SAMPLE_FILE_NAME).exists());
        deleteDirectory(testDir);
    }

    @Test
    public void copyFileTest() throws HyscaleException {
        File testFile = createFile(testFilePath);
        HyscaleFilesUtil.copyFile(sampleFile, testFile);
        assertTrue(testFile.length() == sampleFile.length() ? true : false);
        deleteDirectory(testDir);
    }

    @Test
    public void getFileNameTest() throws HyscaleException {
        assertEquals(TEST_FILE_NAME, HyscaleFilesUtil.getFileName(testFilePath));
    }
    
    
    @Test
    public void listFileInDirTest() {
        File file = createFile(testFilePath);
        List<File> foundFiles = HyscaleFilesUtil.listFilesWithPattern(file.getParent(), TEST_FILE_PATTERN);
        assertEquals(file.getAbsolutePath(), foundFiles.get(0).getAbsolutePath());
        deleteDirectory(testDir);
        foundFiles = HyscaleFilesUtil.listFilesWithPattern(file.getParent(), TEST_FILE_PATTERN);
        assertTrue(foundFiles.isEmpty());
    }
    
    @Test
    public void clearDirectoryTest() throws Exception {
        testDir = createDirectory(testDirPath);
        File testFile = createFile(testFilePath);
        assertTrue(testDir.isDirectory());
        assertTrue(testFile.exists());
        HyscaleFilesUtil.clearDirectory(testDirPath);
        assertFalse(testFile.exists());
        assertTrue(testDir.exists());
        deleteDirectory(testDir);
    }


    @Test
    public void deleteDirectoryTest() throws HyscaleException {
        testDir = createDirectory(testDirPath);
        assertTrue(testDir.isDirectory());
        HyscaleFilesUtil.deleteDirectory(testDirPath);
        assertFalse(testDir.exists());

    }

    @AfterEach
    public void cleanUp() {
        deleteDirectory(testDir);
    }
}
