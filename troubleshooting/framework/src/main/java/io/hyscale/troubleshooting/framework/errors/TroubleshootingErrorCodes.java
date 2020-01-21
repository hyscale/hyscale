package io.hyscale.troubleshooting.framework.errors;

import io.hyscale.commons.exception.HyscaleErrorCode;

public enum TroubleshootingErrorCodes implements HyscaleErrorCode {

    ACTION_NOT_DEFINED("Action {} is not defined"),
    CONDITION_NOT_DEFINED("Condition {} is not defined"),
    CANNOT_INITIALIZE_FLOWCHART("Cannot initialize the flowchart");

    private String message;

    TroubleshootingErrorCodes(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }
}
