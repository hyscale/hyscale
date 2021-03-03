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
import java.util.Arrays;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.commons.io.StringOutputStream;
import io.hyscale.commons.models.CommandResult;

/**
 * Class to execute commands using apache commons exec <a href="https://ommons.apache.org/proper/commons-exec/">apache commons exec</a>
 * Use case:
 * To execute command and check if it was successful
 * To execute command and get output in String
 * To execute command and copy output to a file
 * To execute command in a particular directory
 *
 */
public class CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    private CommandExecutor(){}

    /**
     * @param command to be executed
     * @return {@link CommandResult} which contains command output and exit code
     */
    public static CommandResult executeAndGetResults(String command) {
        return executeAndGetResults(command, null);
    }

    /**
     * Executes command with the given input.
     *
     * @param command
     * @param stdInput it is passed to the command as input
     * @return commandResult  if command execution fails it returns CommandResult with null CommandOutput and exit code as 1.
     */
    public static CommandResult executeAndGetResults(String command, String stdInput) {
        try {
            return execute(command, null, null, stdInput);
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
            commandResult = execute(command, null, null, null);
        } catch (IOException | HyscaleException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_EXECUTE_COMMAND, command);
            logger.error("Failed while executing command, error {}", ex.toString());
            return false;
        }
        return commandResult.getExitCode() == 0;
    }

    /**
     * @param command           to be executed
     * @param commandOutputFile to which command output is redirected
     * @return whether command was successful or not
     */
    public static boolean execute(String command, File commandOutputFile) {
        return executeInDir(command, commandOutputFile, null);
    }
    
    public static boolean executeInDir(String command, String dir) {
        return executeInDir(command, null, dir);
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
            commandResult = execute(command, commandOutputFile, dir, null);
        } catch (IOException | HyscaleException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_EXECUTE_COMMAND, command);
            logger.error("Failed while executing command, error {}", ex.toString());
            return false;
        }
        return commandResult.getExitCode() == 0;
    }

    /**
     * Uses apache commons exec to execute passed command
     * 
     * @param command
     * @param outputFile output stream for process {@link #getOutputStream(File)}
     * @param dir
     * @param stdInput
     * @return {@link CommandResult}
     * @throws HyscaleException
     * @throws IOException
     */
    private static CommandResult execute(String command, File outputFile, String dir, String stdInput)
            throws HyscaleException, IOException {
        
        CommandResult cmdResult = new CommandResult();
        if (StringUtils.isBlank(command)) {
            return cmdResult;
        }
        if (StringUtils.isBlank(dir) || dir.equals(".")) {
            dir = SetupConfig.CURRENT_WORKING_DIR;
        }
        CommandLine commandToExecute = getCommandLine(command);

        Executor executor = new DefaultExecutor();

        /*
         * Stream closing is handled by executor itself
         */
        // Output handler
        OutputStream outputStream = getOutputStream(outputFile);

        // Input handler
        InputStream inputStream = getInputStream(stdInput);

        // Stream handlers
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, outputStream, inputStream);

        // Timeout
        executor.setStreamHandler(streamHandler);
        executor.setWorkingDirectory(new File(dir));
        int exitCode = 1;
        try {
            exitCode = executor.execute(commandToExecute);
        } catch (ExecuteException e) {
            // timeout case
            exitCode = e.getExitValue();
        }
        if (outputFile == null) {
            cmdResult.setCommandOutput(getCommandOutput(outputStream));
        }

        cmdResult.setExitCode(exitCode);
        return cmdResult;
    }

    private static String getCommandOutput(OutputStream outputStream)  {
        if (outputStream instanceof StringOutputStream) {
            return ((StringOutputStream) outputStream).toString();
        }
        return null;
    }
    
    // To process quotes separately, since they are not required 
    private static CommandLine getCommandLine(String command) {

        CommandLine oldCommandLine = CommandLine.parse(command);

        CommandLine newCommandLine = new CommandLine(oldCommandLine.getExecutable());

        Arrays.asList(oldCommandLine.getArguments()).forEach(each -> {
            if (each.startsWith("'") && each.endsWith("'")) {
                each = each.substring(1, each.length() - 1);
            }
            newCommandLine.addArgument(each, false);
        });
        return newCommandLine;
    }

    private static InputStream getInputStream(String stdInput) {
        if (StringUtils.isBlank(stdInput)) {
            return null;
        }
        return new ByteArrayInputStream(stdInput.getBytes(StandardCharsets.UTF_8));
    }

    private static OutputStream getOutputStream(File outputFile) throws FileNotFoundException, HyscaleException {
        if (outputFile != null) {
            HyscaleFilesUtil.createEmptyFile(outputFile);
            return new FileOutputStream(outputFile);
        }
        return new StringOutputStream();
    }

}
