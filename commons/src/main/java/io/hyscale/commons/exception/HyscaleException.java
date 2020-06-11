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

    private HyscaleError hyscaleError;
    private String[] args;
    private Integer code = ToolConstants.HYSCALE_ERROR_CODE;

    public HyscaleException(HyscaleError hyscaleError, String... args) {
        super(hyscaleError.getMessage());
        this.hyscaleError = hyscaleError;
        this.args = args;
    }

    public HyscaleException(HyscaleError hyscaleError) {
        super(hyscaleError.getMessage());
        this.hyscaleError = hyscaleError;
    }

    public HyscaleException(Throwable throwable, HyscaleError hyscaleError) {
        super(hyscaleError.getMessage(), throwable);
        this.hyscaleError = hyscaleError;
    }

    public HyscaleException(Throwable throwable, HyscaleError hyscaleError, String... args) {
        super(hyscaleError.getMessage(), throwable);
        this.hyscaleError = hyscaleError;
        this.args = args;
    }

    public HyscaleException(HyscaleError hyscaleError, Integer code) {
        this.hyscaleError = hyscaleError;
        this.code = code;
    }

    public HyscaleException(HyscaleError hyscaleError, Integer code, String... args) {
        this.hyscaleError = hyscaleError;
        this.code = code;
        this.args = args;
    }

    public HyscaleError getHyscaleError() {
        return hyscaleError;
    }

    public String[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (args != null && args.length != 0 ) {
            sb.append(String.format(hyscaleError.getMessage().replaceAll("\\{\\}", "%s"), args));
        } else {
            sb.append(hyscaleError.getMessage());
        }
        sb.append("]");
        return sb.toString();
    }


    @Override
    public String getMessage() {
        return toString();
    }

    public int getCode() {
        return (hyscaleError.getCode() > 0) ? hyscaleError.getCode() : code;
    }
}
