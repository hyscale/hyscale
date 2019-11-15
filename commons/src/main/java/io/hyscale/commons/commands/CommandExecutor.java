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
package io.hyscale.commons.commands;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.CommandResult;
import io.hyscale.commons.utils.HyscaleFilesUtil;
import io.hyscale.commons.utils.ThreadPoolUtil;

/**
 * Class to execute commands using java process
 * Use case:
 * To execute command and check if it was successful
 * To execute command and get output in String
 * To execute command and copy output to a file
 * To execute command in a particular directory
 * 
 * <p>
 * In case file is not provided, an async thread is started
 * to read copy command output to string writer
 * This is done to ensure output buffer is not exhausted
 * This thread runs until the process is alive
 * String writer is used to get command output when required
 * </p>
 */
@Component
public class CommandExecutor {

	private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

	@Autowired
	private HyscaleFilesUtil filesUtil;

	/**
	 * @param command to be executed
	 * @return {@link CommandResult} which contains command output and exit code
	 */
	public CommandResult executeAndGetResults(String command) {
		CommandResult commandResult = null;
		try {
			commandResult = _execute(command, null, null);
		} catch (IOException | InterruptedException | HyscaleException e) {
			HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_EXECUTE_COMMAND, command);
			logger.error("Failed while executing command, error {}", ex.toString());
		}
		return commandResult;
	}

	/**
	 * @param command to be executed
	 * @return whether command was successful or not
	 */
	public boolean execute(String command) {
		CommandResult commandResult = null;
		try {
			commandResult = _execute(command, null, null);
		} catch (IOException | InterruptedException | HyscaleException e) {
			HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_EXECUTE_COMMAND, command);
			logger.error("Failed while executing command, error {}", ex.toString());
			return false;
		}
		return commandResult == null || commandResult.getExitCode() != 0 ? false : true;
	}

	/**
	 * @param command to be executed
	 * @param commandOutputFile    to which command output is redirected
	 * @return whether command was successful or not
	 */
	public boolean execute(String command, File commandOutputFile) {
		return executeInDir(command, commandOutputFile, null);
	}

	/**
	 * 
	 * @param command to be executed
	 * @param commandOutputFile to which command output is redirected
	 * @param dir in which command is executed
	 * @return whether command was successful or not
	 */
	public boolean executeInDir(String command, File commandOutputFile, String dir) {
		CommandResult commandResult = null;
		try {
			commandResult = _execute(command, commandOutputFile, dir);
		} catch (IOException | InterruptedException | HyscaleException e) {
			HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_EXECUTE_COMMAND, command);
			logger.error("Failed while executing command, error {}", ex.toString());
			return false;
		}
		return commandResult == null || commandResult.getExitCode() != 0 ? false : true;
	}

	/**
	 * Executes command in the directory specified, uses current directory if not
	 * specified If file provided directs output to file (creates if does not exist)
	 * else Asynchronously copy command output in
	 * {@link CommandResult#setCommandOutput(String)} waits for the process to
	 * complete
	 * 
	 * @param command
	 * @param file
	 * @param dir
	 * @return {@link CommandResult}
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws HyscaleException
	 */
	private CommandResult _execute(String command, File file, String dir)
			throws IOException, InterruptedException, HyscaleException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		if (StringUtils.isBlank(dir) || dir.equals(".")) {
			dir = SetupConfig.CURRENT_WORKING_DIR;
		}
		logger.debug("Executing command in dir {}", dir);
		processBuilder.command(command.split(" "));
		processBuilder.redirectErrorStream(true);
		if (file != null) {
			filesUtil.createEmptyFile(file);
			processBuilder.redirectOutput(file);
		}
		Process process = processBuilder.start();
		boolean readOutput = false;
		StringWriter strWriter = new StringWriter();
		if (file == null) {
			readOutput = copyOutput(process, strWriter);
		}
		CommandResult cmdResult = new CommandResult();
		int exitCode = 1;
		try {
			exitCode = process.waitFor();
		} catch (InterruptedException e) {
			logger.error("Error while waiting for process to complete");
			throw e;
		}
		if (readOutput) {
			cmdResult.setCommandOutput(strWriter.toString());
		}
		cmdResult.setExitCode(exitCode);
		return cmdResult;
	}

	/**
	 * Copy output from process input stream to string writer
	 * @param process
	 * @param strWriter
	 * @return is thread started
	 */
	private boolean copyOutput(Process process, StringWriter strWriter) {
		return ThreadPoolUtil.getInstance().execute(() -> {
			try {
				while (process.isAlive()) {
					IOUtils.copy(process.getInputStream(), strWriter, ToolConstants.CHARACTER_ENCODING);
				}
			} catch (IOException e) {
				logger.error("Error while reading command output", e);
			}
		});
	}

}
