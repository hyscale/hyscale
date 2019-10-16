package io.hyscale.ctl.servicespec.commons.exception;

import io.hyscale.ctl.commons.exception.HyscaleErrorCode;

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
