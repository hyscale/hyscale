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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Utility class to handle file operation
 */
@Component
public class HyscaleFilesUtil {

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    
    private static final String NULL_STRING = "null";

    /**
     * Create file in required directory
     *
     * @param filename
     * @param fileData
     * @throws HyscaleException
     */
    public File createFile(String filename, String fileData) throws HyscaleException {
        if (StringUtils.isBlank(filename) || StringUtils.isBlank(fileData)) {
            throw new HyscaleException(CommonErrorCode.FAILED_TO_WRITE_FILE_DATA);
        }
        File file = new File(filename);
        // create parent dir if missing
        file.getParentFile().mkdirs();
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(fileData);
        } catch (IOException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_WRITE_FILE, filename);
            throw ex;
        }
        return file;
    }

    /**
     * Update/Create file in required directory
     *
     * @param filename
     * @param fileData
     * @throws HyscaleException
     */
    public File updateFile(String filename, String fileData) throws HyscaleException {
        if (StringUtils.isBlank(filename) || StringUtils.isBlank(fileData)) {
            throw new HyscaleException(CommonErrorCode.FAILED_TO_WRITE_FILE_DATA);
        }
        File file = new File(filename);
        // create parent dir if missing
        file.getParentFile().mkdirs();
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            fileWriter.write(fileData);
        } catch (IOException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_WRITE_FILE, filename);
            throw ex;
        }
        return file;
    }

    /**
     * Copy file to given directory Makes directory if not exist
     *
     * @param sourceFile
     * @param dest
     * @throws HyscaleException
     */
    public void copyFileToDir(File sourceFile, File dest) throws HyscaleException {
        if (sourceFile == null || !sourceFile.exists()) {
            String[] args = new String[]{sourceFile != null ? sourceFile.getName() : NULL_STRING};
            throw new HyscaleException(CommonErrorCode.FILE_NOT_FOUND, args);
        }
        if (dest == null) {
            throw new HyscaleException(CommonErrorCode.DIRECTORY_REQUIRED_TO_COPY_FILE, sourceFile.getName());
        }
        // create dir if not exist
        dest.mkdirs();
        try {
            FileUtils.copyFileToDirectory(sourceFile, dest);
        } catch (IOException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_COPY_FILE, sourceFile.getName());
            throw ex;
        }
    }

    /**
     * Copy file to destination file Create parent directory if does not exist
     */
    public void copyFile(File sourceFile, File destFile) throws HyscaleException {
        if (sourceFile == null || !sourceFile.exists()) {
            String[] args = new String[]{sourceFile != null ? sourceFile.getName() : NULL_STRING};
            throw new HyscaleException(CommonErrorCode.FILE_NOT_FOUND, args);
        }
        if (destFile == null) {
            throw new HyscaleException(CommonErrorCode.DIRECTORY_REQUIRED_TO_COPY_FILE, sourceFile.getName());
        }
        // Create parent dirs
        destFile.getParentFile().mkdirs();
        try {
            FileUtils.copyFile(sourceFile, destFile);
        } catch (IOException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_COPY_FILE, sourceFile.getName());
            throw ex;
        }
    }

    /**
     * Returns the file name from the given filepath
     *
     * @param filePath
     * @return
     */

    public String getFileName(String filePath) throws HyscaleException {
        if (StringUtils.isBlank(filePath)) {
            throw new HyscaleException(CommonErrorCode.EMPTY_FILE_PATH);
        }

        if (filePath.substring(filePath.length() - 1, filePath.length()).equals(FILE_SEPARATOR)) {
            throw new HyscaleException(CommonErrorCode.FOUND_DIRECTORY_INSTEAD_OF_FILE, filePath);
        }

        int lastDirIndex = filePath.lastIndexOf(FILE_SEPARATOR);
        if (lastDirIndex >= 0) {
            return filePath.substring(lastDirIndex + 1, filePath.length());
        } else {
            return filePath;
        }
    }

    /**
     * Clears directory including internal dirs
     *
     * @param dir
     * @throws HyscaleException
     */
    public void clearDirectory(String dir) throws HyscaleException {
        File directory = new File(dir);
        if (directory.isDirectory()) {
            // true only if file path is directory and exists
            try {
                FileUtils.cleanDirectory(directory);
            } catch (IOException e) {
                HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_CLEAN_DIRECTORY, dir);
                throw ex;
            }
        }
    }

    public void deleteDirectory(String dir) throws HyscaleException {
        File directory = new File(dir);
        if (directory.isDirectory()) {
            // true only if file path is directory and exists
            try {
                FileUtils.deleteDirectory(directory);
            } catch (IOException e) {
                HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_DELETE_DIRECTORY, dir);
                throw ex;
            }
        }
    }
}
