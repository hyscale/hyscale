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

public class NormalizationUtil {

    public static String normalize(String name, int length) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        String normalized = name.toLowerCase().trim().replaceAll("[\\.]+", "-").replaceAll("[ ]+", "-")
                .replaceAll("[^a-zA-Z0-9-_]", "");
        int str_length = normalized.length();
        if (str_length > length) {
            str_length = length;
        }
        return normalized.substring(0, str_length);
    }

    public static String normalize(String name) {
        if (StringUtils.isEmpty(name)) {
            return name;
        }
        int length = name.length();
        return normalize(name, length);
    }

}
