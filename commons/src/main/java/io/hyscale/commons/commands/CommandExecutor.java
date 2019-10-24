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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.commons.models.CommandResult;

@Component
public class CommandExecutor {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);

    public CommandResult executeAndGetResults(String command) {
        CommandResult commandResult = null;
        int exitCode = 1;
        try {
            Process process = executeProcess(command, null, null);
            exitCode = process.waitFor();
            commandResult = new CommandResult();
            commandResult
                    .setCommandOutput(getResult(exitCode == 0 ? process.getInputStream() : process.getErrorStream()));
            commandResult.setExitCode(exitCode);
        } catch (IOException | InterruptedException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_EXECUTE_COMMAND, command);
            logger.error("Failed while executing command, error {}", ex.toString());
        }
        return commandResult;
    }

    public boolean execute(String command) {
        int exitCode = 1;
        try {
            Process process = executeProcess(command, null, null);
            exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_EXECUTE_COMMAND, command);
            logger.error("Failed while executing command, error {}", ex.toString());
            return false;
        }
        return exitCode == 0 ? true : false;
    }

    public boolean execute(String command, File file) {
        return executeInDir(command, file, null);
    }

    public boolean executeInDir(String command, File file, String dir) {
        int exitCode = 1;
        try {
            Process process = executeProcess(command, file, dir);
            exitCode = process.waitFor();
        } catch (IOException | InterruptedException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_EXECUTE_COMMAND, command);
            logger.error("Failed while executing command, error {}", ex.toString());
            return false;
        }
        return exitCode == 0 ? true : false;
    }

    private String getResult(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder output = new StringBuilder();
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            output.append(line);
        }
        return output.toString();
    }

    private Process executeProcess(String command, File file, String dir) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (StringUtils.isBlank(dir) || dir.equals(".")) {
            dir = SetupConfig.CURRENT_WORKING_DIR;
        }
        logger.debug("Executing command in dir {}", dir);
        processBuilder.directory(new File(dir));
        processBuilder.command(command.split(" "));
        processBuilder.redirectErrorStream(true);
        if (file != null) {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            processBuilder.redirectOutput(file);
        }
        return processBuilder.start();
    }
}
