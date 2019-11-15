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

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.config.SetupConfig;
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
public class CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    /**
     * @param command to be executed
     * @return {@link CommandResult} which contains command output and exit code
     */
    public static CommandResult executeAndGetResults(String command) {
        return executeAndGetResults(command, null);
    }

    /**
     * Executes command with the given input,
     * if command execution fails it returns CommandResult with null CommandOutput and exit code as 1.
     *
     * @param command
     * @param stdInput it is passed to the process as input
     * @return commandResult
     */
    public static CommandResult executeAndGetResults(String command, String stdInput) {
        try {
            return _execute(command, null, null, stdInput);
        } catch (Exception e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_EXECUTE_COMMAND, command);
            logger.error("Error while reading command output,error {} for the standard input {}", ex, stdInput);
        }
        CommandResult commandResult = new CommandResult();
        commandResult.setExitCode(1);
        return commandResult;
    }

    /**
     * @param command to be executed
     * @return whether command was successful or not
     */
    public static boolean execute(String command) {
        CommandResult commandResult = null;
        try {
            commandResult = _execute(command, null, null, null);
        } catch (IOException | InterruptedException | HyscaleException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_EXECUTE_COMMAND, command);
            logger.error("Failed while executing command, error {}", ex.toString());
            return false;
        }
        return commandResult == null || commandResult.getExitCode() != 0 ? false : true;
    }

    /**
     * @param command           to be executed
     * @param commandOutputFile to which command output is redirected
     * @return whether command was successful or not
     */
    public static boolean execute(String command, File commandOutputFile) {
        return executeInDir(command, commandOutputFile, null);
    }

    /**
     * @param command           to be executed
     * @param commandOutputFile to which command output is redirected
     * @param dir               in which command is executed
     * @return whether command was successful or not
     */
    public static boolean executeInDir(String command, File commandOutputFile, String dir) {
        CommandResult commandResult = null;
        try {
            commandResult = _execute(command, commandOutputFile, dir, null);
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
     * if standard input is specified, it is passed as input to the process.
     *
     * @param command
     * @param file
     * @param dir
     * @param stdInput
     * @return {@link CommandResult}
     * @throws IOException
     * @throws InterruptedException
     * @throws HyscaleException
     */
    private static CommandResult _execute(String command, File file, String dir, String stdInput)
            throws IOException, InterruptedException, HyscaleException {
        CommandResult cmdResult = new CommandResult();
        int exitCode = 1;
        cmdResult.setExitCode(exitCode);
        if (StringUtils.isBlank(command)) {
            return cmdResult;
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (StringUtils.isBlank(dir) || dir.equals(".")) {
            dir = SetupConfig.CURRENT_WORKING_DIR;
        }
        logger.debug("Executing command in dir {}", dir);
        processBuilder.command(command.split(" "));
        processBuilder.redirectErrorStream(true);
        if (file != null) {
            HyscaleFilesUtil.createEmptyFile(file);
            processBuilder.redirectOutput(file);
        }
        Process process = processBuilder.start();

        try {
            handleStandardInput(process, command, stdInput);
        } catch (HyscaleException e) {
            throw e;
        }

        boolean readOutput = false;
        StringWriter strWriter = new StringWriter();
        if (file == null) {
            readOutput = copyOutput(process, strWriter);
        }
        try {
            exitCode = process.waitFor();
        } catch (
                InterruptedException e) {
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
     *
     * @param process
     * @param strWriter
     * @return is thread started
     */
    private static boolean copyOutput(Process process, StringWriter strWriter) {
        return ThreadPoolUtil.getInstance().execute(() -> {
            try {
                while (process.isAlive()) {
                    IOUtils.copy(process.getInputStream(), strWriter, StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                logger.error("Error while reading command output", e);
            }
        });
    }

    private static void handleStandardInput(Process process, String command, String stdInput) throws HyscaleException {
        if (StringUtils.isNotBlank(stdInput)) {
            try (OutputStream processStdin = process.getOutputStream()) {
                processStdin.write(stdInput.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_WRITE_STDIN, command);
                logger.error("Error while writing std input to the process, error {}", ex.toString());
                throw ex;
            }
        }
    }
}
