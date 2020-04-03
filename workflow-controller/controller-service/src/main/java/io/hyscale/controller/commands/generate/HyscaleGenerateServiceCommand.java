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
package io.hyscale.controller.commands.generate;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import picocli.CommandLine;

/**
 * This class executes 'hyscale generate service' command
 * It is a sub-command of the 'hyscale generate' command
 * @see HyscaleGenerateCommand
 * Every command/sub-command has to implement the {@link Callable} so that
 * whenever the command is executed the {@link #call()}
 * method will be invoked
 *
 * The sub-commands of are handled by @Command annotation
 *
 */
@CommandLine.Command(name = "service", subcommands = { HyscaleGenerateServiceManifestsCommand.class} ,
        description = "Performs action on the service")
@Component
public class HyscaleGenerateServiceCommand implements Callable<Integer> {
    
    @Override
    public Integer call() throws Exception {
        new CommandLine(new HyscaleGenerateServiceCommand()).usage(System.out);
        return ToolConstants.INVALID_INPUT_ERROR_CODE;
    }
}
