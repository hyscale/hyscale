package io.hyscale.generator.services.exception;

import io.hyscale.commons.exception.HyscaleErrorCode;

public enum ManifestErrorCodes implements HyscaleErrorCode {
    ERROR_WHILE_CREATING_MANIFEST("Error while creating manifests "),
    ERROR_WHILE_WRITING_MANIFEST_TO_FILE("Error while writing manifest data to file"),
    ERROR_WHILE_INJECTING_MANIFEST_SNIPPET("Error while injecting manifest snippets to manifest"),
    MISSING_STORAGE_CLASS_FOR_VOLUMES("Missing storage class for volumes {}"),
    INVALID_SIZE_FORMAT("Invalid size format {} "),
    INSUFFIECIENT_MEMORY("Insuffiecient memory {}");

    private String message;

    ManifestErrorCodes(String message) {
        this.message = message;
    }

    @Override
    public String getErrorMessage() {
        return this.message;
    }

}
