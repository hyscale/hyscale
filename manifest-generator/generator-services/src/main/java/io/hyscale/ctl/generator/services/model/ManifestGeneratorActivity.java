package io.hyscale.ctl.generator.services.model;

import io.hyscale.ctl.commons.models.Activity;

public enum ManifestGeneratorActivity implements Activity {
    MISSING_FIELD("Missing field {}"),
    STATEFUL_SET("Generating Stateful Set manifest "),
    DEPLOYMENT("Generating Deployment manifest "),
    ERROR_WHILE_PROCESSING_MANIFEST_PLUGINS("Error while generating manifest plugins "),
    GENERATING_MANIFEST("Generating manifest for {} "),
    INVALID_SIZE_FORMAT("Invalid size format {} "),
    INSUFFICIENT_MEMORY("Ignoring memory limits as 4Mi is minimum memory but declared is {}");

    private String message;

    private ManifestGeneratorActivity(String message) {
        this.message = message;
    }

    @Override
    public String getActivityMessage() {
        return message;
    }

}
