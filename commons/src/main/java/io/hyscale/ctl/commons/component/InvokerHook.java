package io.hyscale.ctl.commons.component;

import io.hyscale.ctl.commons.exception.HyscaleException;

public interface InvokerHook<C> {

	public void preHook(C context) throws HyscaleException;

	public void postHook(C context) throws HyscaleException;

	public void onError(C context, Throwable th);
}
