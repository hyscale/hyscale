package io.hyscale.commons.component;

import io.hyscale.commons.exception.HyscaleException;

/**
 *  This class provides the invocation context
 *  to invoke any component 
 *
 */

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
