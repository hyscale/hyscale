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

import java.util.Base64;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.models.Credentials;

public class EncodeDecodeUtil {

    private EncodeDecodeUtil() {
    }
    
    private static final char DEFAULT_SEPARATOR = ':';

    public static String getEncodedCredentials(String userName, String password) {
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
            return null;
        }
        String tokenString = userName + ToolConstants.COLON + password;
        return Base64.getEncoder().encodeToString(tokenString.getBytes());
    }
    
    /**
     * 
     * @param encodedCredentials should be base64 encoding of username + separator + password
     * @param separator
     * @return Credentials
     */
    public static Credentials getDecodedCredentials(String encodedCredentials, char separator) {
        if (StringUtils.isBlank(encodedCredentials)) {
            return null;
        }
        Credentials credentials = new Credentials();
        String decodedAuth = EncodeDecodeUtil.decode(encodedCredentials);
        int delimiter = decodedAuth.indexOf(separator);
        if (delimiter > 0) {
            credentials.setUsername(decodedAuth.substring(0,delimiter));
            credentials.setPassword(decodedAuth.substring(delimiter+1));
        }
        return credentials;
    }
    
    public static Credentials getDecodedCredentials(String encodedCredentials) {
        return getDecodedCredentials(encodedCredentials, DEFAULT_SEPARATOR);
    }

    public static String decode(String encodedString) {
        if (StringUtils.isBlank(encodedString)) {
            return encodedString;
        }
        return new String(Base64.getDecoder().decode(encodedString));
    }

}
