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
package io.hyscale.commons.commandExecutor;

import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.constants.TestConstants;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.models.CommandResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class CommandExecutorTests {
    private String ECHO_COMMAND = "echo HelloWorld";
    private StringBuilder stringBuilder;

    @BeforeEach
    public void init() {
       stringBuilder  = new StringBuilder();
    }

//    @Test
//    public void testExecuteAndGetResults() {
//        //TODO support for echo command execution
//        CommandResult result = CommandExecutor.executeAndGetResults(ECHO_COMMAND);
//        assertNotNull(result);
//        System.out.println("Assertion output"+result.getCommandOutput());
//        assertEquals(0,result.getExitCode());
//        assertEquals("HelloWorld",result.getCommandOutput());
//    }

//    @Test
//    public void testExecute() {
//        boolean flag = CommandExecutor.execute(ECHO_COMMAND);
//        assertTrue(flag);
//    }
//
//    @Test
//    public void testExecuteWithFile() throws IOException {
//        File testFile = new File(stringBuilder.append(TestConstants.TMP_DIR).append(ToolConstants.FILE_SEPARATOR).append("testExecute").toString());
//        testFile.createNewFile();
//        boolean flag = CommandExecutor.execute(ECHO_COMMAND, testFile);
//        assertTrue(testFile.exists());
//        assertEquals(ECHO_COMMAND.split(" ")[1], FileUtils.readFileToString(testFile,"UTF-8").trim());
//        assertTrue(flag);
//        testFile.delete();
//    }
//
    @Test
    public void testExecuteInDir() {
        StringBuilder command = new StringBuilder();
        if (SystemUtils.IS_OS_WINDOWS) {
            command.append("cmd.exe /c mkdir testDirectory");
        } else {
            command.append("mkdir testDirectory");
        }
        File testFile = new File(TestConstants.TMP_DIR+ToolConstants.FILE_SEPARATOR+"testFile");
        boolean flag = CommandExecutor.executeInDir(command.toString(), testFile,TestConstants.TMP_DIR);
        assertTrue(testFile.exists());
        assertTrue(flag);
        File testDir = new File(System.getProperty("user.dir")+ToolConstants.FILE_SEPARATOR+"testDirectory");
        assertTrue(testDir.isDirectory());
        assertTrue(testDir.exists());
        testFile.delete();
    }
}
