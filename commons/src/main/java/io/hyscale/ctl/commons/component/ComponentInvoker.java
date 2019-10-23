package io.hyscale.ctl.commons.component;

import java.util.ArrayList;
import java.util.List;

import io.hyscale.ctl.commons.exception.HyscaleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Any class can extend this class to process.
 * Hooks can be implemented using @see {@link InvokerHook} and register
 * with component @see {@link #addHook(InvokerHook)}
 *
 * @param <C> <p> Implementation Notes </p>
 * @see #doExecute(ComponentInvokerContext) to execute any process . This method
 * will be invoked after all @see {@link InvokerHook#preHook(Object)}
 * After successful execution all @see {@link InvokerHook#postHook(Object)}
 * are executed. In case of error the execution is terminated and the
 * @see {@link #onError(ComponentInvokerContext, HyscaleException)} is invoked.
 */

public abstract class ComponentInvoker<C extends ComponentInvokerContext> {

    private static final Logger logger = LoggerFactory.getLogger(ComponentInvoker.class);

    private List<InvokerHook> hooks = new ArrayList<InvokerHook>();

    protected void addHook(InvokerHook hook) {
        this.hooks.add(hook);
    }

    public void execute(C context) {
        try {
            if (hooks == null || hooks.isEmpty()) {
                operate(context);
            } else {
                executeHooks(true, context);
                operate(context);
                executeHooks(false, context);
            }
        } catch (HyscaleException e) {
            onError(context, e);
        }
    }

    private void executeHooks(boolean before, C context) {
        for (InvokerHook hook : hooks) {
            if (context == null || context.isFailed()) {
                logger.error("Cannot execute the hook {}", hook.getClass());
                return;
            }
            try {
                if (before) {
                    hook.preHook(context);
                } else {
                    hook.postHook(context);
                }
            } catch (HyscaleException he) {
                context.setHyscaleException(he);
                hook.onError(context, he);
            }
        }
    }

    protected abstract void doExecute(C context) throws HyscaleException;

    protected abstract void onError(C context, HyscaleException th);

    private boolean operate(C context) throws HyscaleException {
        if (context == null || context.isFailed()) {
            logger.error("Cannot execute the component {}", getClass());
            onError(context, context.getHyscaleException());
            return false;
        }
        doExecute(context);
        return true;
    }

}
