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
package io.hyscale.controller.piccoli;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.IHelpSectionRenderer;

/**
 * This class provides workaround to overcome cases not handled by picocli
 * Such as picocli doesn't differentiate between case sensitive args
 * TODO remove this once Picocli supports case sensitive options
 * @see https://github.com/remkop/picocli/issues/154 
 * 
 * @author tushar
 *
 */
public class ProfileArgsManipulator {

    public static final String PROFILE_DIR_OPTION = "-P";

    public static final String TEMP_PROFILE_DIR_OPTION = "-z";

    /**
     * Update commands args to replace {@link #PROFILE_DIR_OPTION} with {link #TEMP_PROFILE_DIR_OPTION}
     * Commands then read {@link #TEMP_PROFILE_DIR_OPTION} for input
     * TODO remove this once Picocli supports case sensitive options
     * @param args 
     * @return args after replacing required values
     */
    public static String[] updateArgs(String[] args) {
        if (args == null) {
            return args;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith(PROFILE_DIR_OPTION)) {
                args[i] = args[i].replace(PROFILE_DIR_OPTION, TEMP_PROFILE_DIR_OPTION);
            }
        }
        return args;
    }

    /**
     * Update commands help args to replace {link #TEMP_PROFILE_DIR_OPTION} with {@link #PROFILE_DIR_OPTION}
     * Ensures user is shown the right option in help message
     * TODO remove once Picocli supports case sensitive options
     * @param commandLine
     * @return map of help message
     */
    public static Map<String, IHelpSectionRenderer> updateHelp(CommandLine commandLine) {
        Map<String, IHelpSectionRenderer> helpSectionMap = commandLine.getHelpSectionMap();
        helpSectionMap.put(CommandLine.Model.UsageMessageSpec.SECTION_KEY_SYNOPSIS, new IHelpSectionRenderer() {
            @Override
            public String render(Help help) {
                String message = help.synopsis(help.synopsisHeadingLength());
                return message != null ? message.replaceAll(TEMP_PROFILE_DIR_OPTION, PROFILE_DIR_OPTION) : message;
            }
        });
        helpSectionMap.put(CommandLine.Model.UsageMessageSpec.SECTION_KEY_OPTION_LIST, new IHelpSectionRenderer() {
            @Override
            public String render(Help help) {
                String message = help.optionList();
                return message != null ? message.replaceAll(TEMP_PROFILE_DIR_OPTION, PROFILE_DIR_OPTION) : message;
            }
        });
        return helpSectionMap;
    }

    /**
     * 
     * @param message
     * @return message where {@link #TEMP_PROFILE_DIR_OPTION} is replaced by {@link #PROFILE_DIR_OPTION}
     */
    public static String updateMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return message;
        }
        return message.replaceAll(TEMP_PROFILE_DIR_OPTION, PROFILE_DIR_OPTION);
    }
}
