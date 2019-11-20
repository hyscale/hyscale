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

public enum CommonErrorCode implements HyscaleErrorCode {
    FAILED_TO_EXECUTE_COMMAND("Failed to execute command {}"),
    FAILED_TO_WRITE_STDIN("Failed to write standard input to the process"),
    FAILED_TO_COPY_FILE("Failed to copy the file {}"),
    DIRECTORY_REQUIRED_TO_COPY_FILE("Directory required to copy file {}"),
    FILE_NOT_FOUND("File {} not found"),
    FAILED_TO_RESOLVE_TEMPLATE("Failed to resolve {} template"),
    FAILED_TO_WRITE_FILE("Failed to write to file {}"),
    FAILED_TO_WRITE_FILE_DATA("Failed to write data into file {}"),
    SERVICE_SPEC_REQUIRED("Service spec required"),
    EMPTY_FILE_PATH("Empty file found"),
    FAILED_TO_CLEAN_DIRECTORY("Failed to clean directory {}"),
    FAILED_TO_DELETE_DIRECTORY("Failed to delete the directory {}"),
    FOUND_DIRECTORY_INSTEAD_OF_FILE("Found directory {} instead of file");

    private String message;

    CommonErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
