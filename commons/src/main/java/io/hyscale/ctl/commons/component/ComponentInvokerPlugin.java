package io.hyscale.ctl.commons.component;

import io.hyscale.ctl.commons.exception.HyscaleException;

public interface ComponentInvokerPlugin<C> {

	public void doBefore(C context) throws HyscaleException;

	public void doAfter(C context) throws HyscaleException;

	public void onError(C context, Throwable th);
}
