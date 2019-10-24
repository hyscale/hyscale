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

/**
 * Error codes for Service Spec related operations
 *
 */
public enum ServiceSpecErrorCodes implements HyscaleErrorCode {

    SERVICE_SPEC_PARSE_ERROR("Failed to parse service spec"),
    SERVICE_PROFILE_PARSE_ERROR("Failed to parser service profile"),
    FAILED_TO_PARSE_JSON_TREE("Failed to parse json tree"),
    ERROR_WHILE_FETCH_SERVICE_SPEC_FIELD("Failed to fetch service spec field {}"),
    CANNOT_PROCESS_SERVICE_SPEC("Cannot process service spec");

    private String message;

    ServiceSpecErrorCodes(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
