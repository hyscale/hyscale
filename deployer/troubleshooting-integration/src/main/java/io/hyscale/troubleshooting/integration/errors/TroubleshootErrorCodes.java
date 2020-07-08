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
package io.hyscale.troubleshooting.integration.errors;

import io.hyscale.commons.exception.HyscaleError;

public enum TroubleshootErrorCodes implements HyscaleError {

    ERROR_WHILE_BUILDING_RESOURCES("Error while building resources for troubleshooting service {}"),
    INVALID_ACTION("Action is not applicable for this resource"),
    SERVICE_IS_NOT_DEPLOYED("Service {} is not deployed in the cluster "),
    CANNOT_DETERMINE_CAUSE_OF_THE_ERROR("Cannot determine the cause of the problem");

    private String message;
    private int code;
    TroubleshootErrorCodes(String message) {
        this.message = message;
    }

    TroubleshootErrorCodes(String message,Integer code){
        this.message=message;
        this.code=code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getCode() {
        return this.code;
    }
}
