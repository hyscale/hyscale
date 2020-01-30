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
    FAILED_TO_GET_USER_INPUT("Failed to get user input"),
    INVALID_INPUT_BY_USER("User provided invalid input {}"),
    FAILED_TO_EXECUTE_COMMAND("Failed to execute command {}"),
    FAILED_TO_READ_FILE("Failed to read file {}"),
    FAILED_TO_WRITE_STDIN("Failed to write standard input to the process"),
    FAILED_TO_COPY_FILE("Failed to copy the file {}"),
    DIRECTORY_REQUIRED_TO_COPY_FILE("Directory required to copy file {}"),
    FILE_NOT_FOUND("File {} not found"),
    LOGFILE_NOT_FOUND("Failed to log due to invalid file path."),
    FAILED_TO_RESOLVE_TEMPLATE("Failed to resolve {} template"),
    FAILED_TO_WRITE_FILE("Failed to write to file {}"),
    FAILED_TO_WRITE_FILE_DATA("Failed to write data into file {}"),
    SERVICE_SPEC_REQUIRED("Service spec required"),
    EMPTY_FILE_PATH("Empty file found"),
    FAILED_TO_CLEAN_DIRECTORY("Failed to clean directory {}"),
    FAILED_TO_DELETE_DIRECTORY("Failed to delete the directory {}"),
    FOUND_DIRECTORY_INSTEAD_OF_FILE("Found directory {} instead of file"),
    TEMPLATE_CONTEXT_NOT_FOUND("Template Context not found for template {}"),
    INPUTSTREAM_NOT_FOUND("Cannot find inputstream and so cannot write to logfile"),
    FAILED_TO_READ_LOGFILE("Failed to read logs at log file {}"),
    OUTPUTSTREAM_NOT_FOUND("Cannot find output stream and so cannot write to stream"),
    STRATEGIC_MERGE_KEY_NOT_FOUND("Merge key not found while merging {}"),
    INVALID_JSON_FORMAT("Json format is invalid");
    
    private String message;

    CommonErrorCode(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
