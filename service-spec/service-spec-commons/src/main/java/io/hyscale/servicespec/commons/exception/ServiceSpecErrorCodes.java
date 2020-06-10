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
package io.hyscale.servicespec.commons.exception;

import io.hyscale.commons.exception.HyscaleErrorCode;
import io.hyscale.commons.exception.HyscaleErrorGroup;

/**
 * Error codes for Service Spec related operations
 *
 */
public enum ServiceSpecErrorCodes implements HyscaleErrorCode {

    SERVICE_SPEC_REQUIRED("Service spec required",HyscaleErrorGroup.SERVICE_SPEC_PROCESSING),
    SERVICE_SPEC_PARSE_ERROR("Failed to parse service spec",HyscaleErrorGroup.SERVICE_SPEC_PROCESSING),
    SERVICE_PROFILE_REQUIRED("Service profile required",HyscaleErrorGroup.SERVICE_SPEC_PROCESSING),
    SERVICE_PROFILE_PARSE_ERROR("Failed to parser service profile",HyscaleErrorGroup.SERVICE_SPEC_PROCESSING),
    FAILED_TO_PARSE_JSON_TREE("Failed to parse json tree",HyscaleErrorGroup.SERVICE_SPEC_PROCESSING),
    ERROR_WHILE_FETCH_SERVICE_SPEC_FIELD("Failed to fetch service spec field {}"),
    INVALID_FILE_EXTENSION("Invalid file extension:{} given."),
    INVALID_FORMAT("Invalid format {}."),
    MISSING_FIELD_IN_PROFILE_FILE("Cannot find {} in the hprof file.",HyscaleErrorGroup.SERVICE_PROFILE_PROCESSING),
    MISSING_FIELD_IN_SERVICE_FILE("Cannot find {} in the hspec file.",HyscaleErrorGroup.SERVICE_SPEC_PROCESSING),
    INPUT_DATA_MISMATCH("Service or env name in the file name did not match with names specified in the input file.");


    private String message;
    private int code;

    ServiceSpecErrorCodes(String message) {
        this.message = message;
    }

    ServiceSpecErrorCodes(String message, HyscaleErrorGroup errorGroup){
        this.message=message;
        this.code=errorGroup.getGroupCode();
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }
    @Override
    public int getErrorCode() {
        return this.code;
    }

}
