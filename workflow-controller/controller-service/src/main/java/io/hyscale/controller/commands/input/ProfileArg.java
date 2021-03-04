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
package io.hyscale.controller.commands.input;

import java.io.File;
import java.util.List;

import io.hyscale.controller.commands.args.FileConverter;
import picocli.CommandLine;

/**
 * Provides profile input options
 * @author tushar
 *
 */
public class ProfileArg {

    @CommandLine.Option(names = { "-p", "--profile" }, 
            required = false, description = "Profile for service.", converter = FileConverter.class)
    private List<File> profiles;

    @CommandLine.Option(names = {"-P"}, required = false, description = "Profile name for service.")
    private String profileName;

    public List<File> getProfiles() {
        return profiles;
    }

    public String getProfileName() {
        return profileName;
    }

}