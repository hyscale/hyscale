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
package io.hyscale.commons.utils;

import org.apache.commons.lang3.StringUtils;

public class HyscaleStringUtil {

    private HyscaleStringUtil() {
    }

    public static String removeSuffixStr(String input, String trailingStr) {
        if (StringUtils.isBlank(input) || StringUtils.isBlank(trailingStr)) {
            return input;
        }
        if (input.endsWith(trailingStr)) {
            return input.substring(0, input.lastIndexOf(trailingStr));
        }
        return input;
    }

    public static String removeSuffixStr(StringBuilder input, String trailingStr) {
        if (input == null) {
            return null;
        }
        return removeSuffixStr(input.toString(), trailingStr);
    }

    public static String removeSuffixChar(StringBuilder input, char trailingChar) {
        if (input == null) {
            return null;
        }
        return removeSuffixStr(input.toString(), Character.toString(trailingChar));
    }


    public static String getPlural(String word) {
        String consonants = "bcdfghjklmnpqrstvwxyzs";
        boolean hasConsonant = consonants.contains(String.valueOf(word.charAt(word.length() - 2)));
        String lastLetter = word.substring(0, word.length() - 1);
        if (word.endsWith("s") || word.endsWith("x") || word.endsWith("z") || word.endsWith("ch") || word.endsWith("sh")) {
            return word + "es";
        }
        if (word.endsWith("y") && hasConsonant) {
            return lastLetter + "ies";
        }
        if (word.endsWith("f")) {
            return lastLetter + "ves";
        }
        if (word.endsWith("fe")) {
            return word.substring(0, word.length() - 2) + "ves";
        }
        if (word.endsWith("o") && hasConsonant) {
            return word + "es";
        }
        return word + "s";
    }

}
