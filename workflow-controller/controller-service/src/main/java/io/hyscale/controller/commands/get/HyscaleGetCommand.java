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
package io.hyscale.controller.commands.get;

import java.util.concurrent.Callable;

import io.hyscale.controller.commands.HyscaleCommand;
import io.hyscale.controller.commands.get.app.HyscaleGetAppsCommand;
import io.hyscale.controller.commands.get.replica.HyscaleGetReplicaCommand;
import io.hyscale.controller.commands.get.service.HyscaleGetServiceCommand;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * This class executes 'hyscale get' command
 * It is a sub-command of the 'hyscale' command
 * @see HyscaleCommand
 * Every command/sub-command has to implement the {@link Callable} so that
 * whenever the command is executed the {@link #call()}
 * method will be invoked
 *
 * The sub-commands of are handled by @Command annotation
 *
 */
@Command(name = "get", subcommands = {HyscaleGetServiceCommand.class,
        HyscaleGetAppsCommand.class, HyscaleGetReplicaCommand.class},
        description = "Gets the specified resource.")
@Component
public class HyscaleGetCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
    private boolean helpRequested = false;

    /**
     * Executes the 'hyscale get' command
     * Provides usage of this command to the user
     */
    @Override
    public Integer call() throws Exception {
        new CommandLine(this).usage(System.out);
        return ToolConstants.INVALID_INPUT_ERROR_CODE;
    }

}
