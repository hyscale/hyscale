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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public class TimeStampProviderTests {
    private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String TEST_PATTERN = "dd MMMM yyyy";

    public static Stream<Arguments> getPattern() {
        return Stream.of(null,
                Arguments.of(TEST_PATTERN));
    }

    @ParameterizedTest
    @MethodSource(value = "getPattern")
    public void getTimeStampTest(String pattern) {
        String time = TimeStampProvider.get(pattern);
        if(pattern == null){
            pattern= DEFAULT_DATE_PATTERN;
        }
        Assertions.assertTrue(StringUtils.isNotEmpty(time));
        Assertions.assertTrue(matchPattern(time, pattern));
    }

    public boolean matchPattern(String time, String pattern) {
        DateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setLenient(false);
        try {
            Date date = formatter.parse(time);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }
}


