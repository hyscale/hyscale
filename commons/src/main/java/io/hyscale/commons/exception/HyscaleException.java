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
package io.hyscale.commons.exception;

import io.hyscale.commons.constants.ToolConstants;

public class HyscaleException extends Exception {

    private HyscaleErrorCode hyscaleErrorCode;
    private String[] args;
    private Integer code = ToolConstants.HYSCALE_ERROR_CODE;

    public HyscaleException(HyscaleErrorCode hyscaleErrorCode, String... args) {
        super(hyscaleErrorCode.getErrorMessage());
        this.hyscaleErrorCode = hyscaleErrorCode;
        this.args = args;
    }

    public HyscaleException(HyscaleErrorCode hyscaleErrorCode) {
        super(hyscaleErrorCode.getErrorMessage());
        this.hyscaleErrorCode = hyscaleErrorCode;
    }

    public HyscaleException(Throwable throwable, HyscaleErrorCode hyscaleErrorCode) {
        super(hyscaleErrorCode.getErrorMessage(), throwable);
        this.hyscaleErrorCode = hyscaleErrorCode;
    }

    public HyscaleException(Throwable throwable, HyscaleErrorCode hyscaleErrorCode, String... args) {
        super(hyscaleErrorCode.getErrorMessage(), throwable);
        this.hyscaleErrorCode = hyscaleErrorCode;
        this.args = args;
    }

    public HyscaleException(HyscaleErrorCode hyscaleErrorCode, Integer code) {
        this.hyscaleErrorCode = hyscaleErrorCode;
        this.code = code;
    }

    public HyscaleException(HyscaleErrorCode hyscaleErrorCode, Integer code, String... args) {
        this.hyscaleErrorCode = hyscaleErrorCode;
        this.code = code;
        this.args = args;
    }

    public HyscaleErrorCode getHyscaleErrorCode() {
        return hyscaleErrorCode;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (args != null && args.length != 0 ) {
            sb.append(String.format(hyscaleErrorCode.getErrorMessage().replaceAll("\\{\\}", "%s"), args));
        } else {
            sb.append(hyscaleErrorCode.getErrorMessage());
        }
        sb.append("]");
        return sb.toString();
    }


    @Override
    public String getMessage() {
        return toString();
    }

    public int getCode() {
        return code;
    }
}
