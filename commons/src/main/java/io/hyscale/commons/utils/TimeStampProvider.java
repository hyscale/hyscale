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

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeStampProvider {

    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private TimeStampProvider() {}

    public static String get() {
        return get(DEFAULT_DATE_PATTERN);
    }

    public static String getInMillis() {
        return Long.toString(System.currentTimeMillis());
    }

    public static String get(String pattern) {
        pattern = pattern == null ? DEFAULT_DATE_PATTERN : pattern;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }
}
