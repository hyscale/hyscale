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
package io.hyscale.commons.component;

import java.util.ArrayList;
import java.util.List;

import io.hyscale.commons.exception.HyscaleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Any class can extend this class to process.
 * Hooks can be implemented using @see {@link InvokerHook} and register
 * with component @see {@link #addHook(InvokerHook)}
 *
 * @param <C> <p> Implementation Notes </p>
 * @see #doExecute(ComponentInvokerContext) to execute any process . This method
 * will be invoked after all {@link InvokerHook#preHook(Object)}.
 * After successful execution all {@link InvokerHook#postHook(Object)}
 * are executed. In case of error the execution is terminated, then
 * {@link #onError(ComponentInvokerContext, HyscaleException)} is invoked.
 */
public abstract class ComponentInvoker<C extends ComponentInvokerContext> {

    private static final Logger logger = LoggerFactory.getLogger(ComponentInvoker.class);

    private List<InvokerHook<C>> hooks = new ArrayList<>();

    protected void addHook(InvokerHook<C> hook) {
        this.hooks.add(hook);
    }

    public void execute(C context) throws HyscaleException {
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
        for (InvokerHook<C> hook : hooks) {
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

    protected abstract void onError(C context, HyscaleException th) throws HyscaleException;

    private boolean operate(C context) throws HyscaleException {
        if (context == null || context.isFailed()) {
            logger.error("Cannot execute the component {}", getClass());
            if (context != null && context.getHyscaleException() != null) {
                throw context.getHyscaleException();
            }
            return false;
        }
        doExecute(context);
        return true;
    }

}
