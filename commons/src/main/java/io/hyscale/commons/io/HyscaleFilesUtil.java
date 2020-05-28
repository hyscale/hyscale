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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;

/**
 * Utility class to handle file operation
 */
public class HyscaleFilesUtil {

	private static final Logger logger = LoggerFactory.getLogger(HyscaleFilesUtil.class);

	private static final String NULL_STRING = "null";

	/**
	 * Create file in required directory
	 *
	 * @param filename
	 * @param fileData
	 * @throws HyscaleException
	 */
	public static File createFile(String filename, String fileData) throws HyscaleException {
		if (StringUtils.isBlank(filename)) {
			throw new HyscaleException(CommonErrorCode.FAILED_TO_WRITE_FILE_DATA);
		}
		File file = createEmptyFile(filename);
		if (StringUtils.isBlank(fileData)) {
			logger.debug("Created empty file {}", filename);
			return file;
		}

		try (FileWriter fileWriter = new FileWriter(file)) {
			fileWriter.write(fileData);
		} catch (IOException e) {
			HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_WRITE_FILE, filename);
			throw ex;
		}
		return file;
	}

	/**
	 * Create empty file with name
	 * 
	 * @param filename
	 * @return created file
	 * @throws HyscaleException if failed to create file
	 */
	public static File createEmptyFile(String filename) throws HyscaleException {
		File file = new File(filename);
		return createEmptyFile(file);
	}

	/**
	 * Creates empty file if does not exist
	 * @param file
	 * @return file
	 * @throws HyscaleException
	 */
	public static File createEmptyFile(File file) throws HyscaleException {
		if (file == null) {
			return null;
		}
		if (file.exists()) {
			return file;
		}
		file.getParentFile().mkdirs();
		try {
			file.createNewFile();
		} catch (IOException e) {
			HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_WRITE_FILE, file.getName());
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
	public static File updateFile(String filename, String fileData) throws HyscaleException {
		if (StringUtils.isBlank(filename) || StringUtils.isBlank(fileData)) {
			throw new HyscaleException(CommonErrorCode.FAILED_TO_WRITE_FILE_DATA);
		}
		File file = new File(filename);
		// create parent dir if missing
		file.getParentFile().mkdirs();
		try (FileWriter fileWriter = new FileWriter(file, true)) {
			fileWriter.write(fileData);
		} catch (IOException e) {
			logger.error("Failed to update file {}", e);
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
	public static void copyFileToDir(File sourceFile, File dest) throws HyscaleException {
		if (sourceFile == null || !sourceFile.exists()) {
			String[] args = new String[] { sourceFile != null ? sourceFile.getName() : NULL_STRING };
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
	public static void copyFile(File sourceFile, File destFile) throws HyscaleException {
		if (sourceFile == null || !sourceFile.exists()) {
			String[] args = new String[] { sourceFile != null ? sourceFile.getName() : NULL_STRING };
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

	public static String getFileName(String filePath) throws HyscaleException {
		if (StringUtils.isBlank(filePath)) {
			throw new HyscaleException(CommonErrorCode.EMPTY_FILE_PATH);
		}

		if (filePath.substring(filePath.length() - 1, filePath.length()).equals(ToolConstants.FILE_SEPARATOR)) {
			throw new HyscaleException(CommonErrorCode.FOUND_DIRECTORY_INSTEAD_OF_FILE, filePath);
		}

		int lastDirIndex = filePath.lastIndexOf(ToolConstants.FILE_SEPARATOR);
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
	public static void clearDirectory(String dir) throws HyscaleException {
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

	public static void deleteDirectory(String dir) throws HyscaleException {
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
	
	public static String readFileData(File filepath) throws HyscaleException {
	    if (filepath == null) {
	        return null;
	    }
	    if (!filepath.exists()) {
	        logger.debug("File {} does not exist, returning null data", filepath);
	        return null;
	    }
	    try (FileInputStream inputStream = new FileInputStream(filepath)){
	        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_READ_FILE, filepath.getAbsolutePath());
            throw ex;
        }
	}
	
	/**
	 * List all files present in directory which matches filename regex pattern
	 * @param directory
	 * @param fileNamePattern
	 * @return List of files
	 */
	public static List<File> listFilesWithPattern(String directory, String fileNamePattern) {
        if (directory == null || StringUtils.isBlank(fileNamePattern)) {
            return null;
        }
        return listFilesWithPattern(new File(directory), fileNamePattern);
    }
	
	/**
     * List all files present in directory which matches filename regex pattern
     * @param directory
     * @param fileNamePattern
     * @return List of files
     */
	public static List<File> listFilesWithPattern(File directory, String fileNamePattern){
	    if (directory == null || !directory.isDirectory()|| StringUtils.isBlank(fileNamePattern)) {
            return null;
        }
	    
	    File[] matchingFile = directory.listFiles((dir, name) -> {
	          return name.matches(fileNamePattern);
	        });
	    
	    return matchingFile == null ? null : Arrays.asList(matchingFile);
	}
	
}