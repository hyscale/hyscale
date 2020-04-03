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
package io.hyscale.controller.commands.get.replica;

import io.hyscale.commons.constants.ToolConstants;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "replica", aliases = "replicas",
        subcommands = {HyscaleReplicaStatusCommand.class}, description = "Get the replica sub-resource of the service")
@Component
public class HyscaleGetReplicaCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
    private boolean helpRequested = false;

    /**
     * Executes the 'hyscale get replica' command
     * Provides usage of this command to the user
     */
    @Override
    public Integer call() throws Exception {
        new CommandLine(new HyscaleGetReplicaCommand()).usage(System.out);
        return ToolConstants.INVALID_INPUT_ERROR_CODE;
    }
}
