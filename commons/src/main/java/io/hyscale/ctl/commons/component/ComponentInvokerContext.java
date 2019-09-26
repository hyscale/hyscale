package io.hyscale.ctl.commons.component;

import io.hyscale.ctl.commons.exception.HyscaleException;

public class ComponentInvokerContext {

    private boolean failed;
    private HyscaleException hyscaleException;

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public HyscaleException getHyscaleException() {
        return hyscaleException;
    }

    public void setHyscaleException(HyscaleException hyscaleException) {
        this.hyscaleException = hyscaleException;
    }
}
