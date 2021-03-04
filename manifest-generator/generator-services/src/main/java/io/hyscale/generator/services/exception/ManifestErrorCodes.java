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
package io.hyscale.generator.services.exception;

import io.hyscale.commons.exception.HyscaleError;
import io.hyscale.commons.exception.HyscaleErrorGroup;

public enum ManifestErrorCodes implements HyscaleError {
    ERROR_WHILE_CREATING_MANIFEST("Error while creating manifests ",HyscaleErrorGroup.MANIFEST_GENERATION),
    ERROR_WHILE_WRITING_MANIFEST_TO_FILE("Error while writing manifest data to file",HyscaleErrorGroup.MANIFEST_GENERATION),
    ERROR_WHILE_INJECTING_MANIFEST_SNIPPET("Error while injecting manifest snippets to manifest"),
    MISSING_STORAGE_CLASS_FOR_VOLUMES("Missing storage class for volumes {}"),
    INVALID_SIZE_FORMAT("Invalid size format {} "),
    INSUFFICIENT_MEMORY("Insufficient memory {}"),
    INVALID_FORMAT_CPUTHRESHOLD("Invalid format for cpuThreshold , use : <number>% "),
    ERROR_WHILE_APPLYING_CUSTOM_SNIPPETS("Error while applying K8s Snippets");

    private String message;
    private int code;

    ManifestErrorCodes(String message) {
        this.message = message;
    }

    ManifestErrorCodes(String message, HyscaleErrorGroup errorGroup){
        this.message=message;
        this.code=errorGroup.getGroupCode();
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public int getCode() {
        return this.code;
    }

}
