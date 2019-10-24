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

import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * This class is the first level command for hyscale tool.
 * This class executes 'hyscale' command
 *
 * Every command/sub-command has to implement the Runnable so that
 * whenever the command is executed the {@link #run()}
 * method will be invoked
 * <p>
 *
 * Command annotation overrides the version provider that picoli provides
 * by default, with {@link HyscaleVersionProvider} as implementation
 *
 * Also check the sub-commands at @Command annotation
 * </p>
 */
@Command(name = "hyscale", versionProvider = HyscaleVersionProvider.class, mixinStandardHelpOptions = true, subcommands = {
        HyscaleGetCommand.class, HyscaleDeployCommand.class, HyscaleUndeployCommand.class,
        HyscaleGenerateCommand.class})
@Component
public class HyscaleCtlCommand implements Runnable {

    /**
     * Executes the hyscale command
     * Provides usage of this command to the user.
     */
    @Override
    public void run() {
        new CommandLine(new HyscaleCtlCommand()).usage(System.out);
    }

}
