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
package io.hyscale.controller.commands;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * This class executes 'hyscale deploy' command
 * It is a sub-command of the 'hyscale' command
 * @see HyscaleCommand
 * Every command/sub-command has to implement the {@link Callable} so that
 * whenever the command is executed the {@link #call()}
 * method will be invoked
 *
 * The sub-commands of are handled by @Command annotation
 *
 */
@Command(name = "deploy", subcommands = { HyscaleDeployServiceCommand.class}, description = "Deploys the specified resource")
@Component
public class HyscaleDeployCommand implements Callable<Integer> {

	@CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
	private boolean helpRequested = false;

	/**
	 * Executes the 'hyscale deploy' command
	 * Provides usage of this command to the user
	 */
	@Override
    public Integer call() throws Exception {
		new CommandLine(new HyscaleDeployCommand()).usage(System.out);
		return ToolConstants.INVALID_INPUT_ERROR_CODE;
	}
}
