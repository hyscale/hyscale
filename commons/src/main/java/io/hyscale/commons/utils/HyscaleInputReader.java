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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;

/**
 * Class to read user provided input
 * @author tushar
 *
 */
public class HyscaleInputReader {

    private static final Logger logger = LoggerFactory.getLogger(HyscaleInputReader.class);

    public static final Integer MAX_RETRIES = 2;

    private static final InputStream DEFAULT_INPUT_STREAM = System.in;

    public static String readInput() throws HyscaleException {
        return readInput(null);
    }

    public static String readInput(InputStream is) throws HyscaleException {
        if (is == null) {
            is = DEFAULT_INPUT_STREAM;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            return br.readLine();
        } catch (Exception e) {
            logger.error("Error while getting valid input", e);
            throw new HyscaleException(e, CommonErrorCode.FAILED_TO_GET_VALID_INPUT);
        }
    }

}
