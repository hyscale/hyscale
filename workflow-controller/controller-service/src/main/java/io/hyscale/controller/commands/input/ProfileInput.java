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

import io.hyscale.controller.util.CommandUtil;
import picocli.CommandLine;

public class ProfileInput {

    @CommandLine.Option(names = {"-p", "--profile"}, required = false, description = "Profile for service.")
    private List<File> profiles;
    
    @CommandLine.Option(names = CommandUtil.TEMP_PROFILE_DIR_OPTION, required = false, description = "Profile name for service.")
    private String profileName;

    public List<File> getProfiles() {
        return profiles;
    }

    public String getProfileName() {
        return profileName;
    }
    
    
}