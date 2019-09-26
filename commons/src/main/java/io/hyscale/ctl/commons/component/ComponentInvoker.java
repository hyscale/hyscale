package io.hyscale.ctl.commons.component;

import java.util.ArrayList;
import java.util.List;

import io.hyscale.ctl.commons.exception.HyscaleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComponentInvoker<C extends ComponentInvokerContext> {

    private static final Logger logger = LoggerFactory.getLogger(ComponentInvoker.class);

    private List<ComponentInvokerPlugin> plugins = new ArrayList<ComponentInvokerPlugin>();

    protected void addPlugin(ComponentInvokerPlugin plugin) {
        this.plugins.add(plugin);
    }

    public void execute(C context) {
        try {
            if (plugins == null || plugins.isEmpty()) {
                operate(context);
            } else {
                executePlugins(true, context);
                operate(context);
                executePlugins(false, context);
            }
        } catch (HyscaleException e) {
            onError(context, e);
        }
    }

    private void executePlugins(boolean before, C context) {
        for (ComponentInvokerPlugin plugin : plugins) {
            if (context == null || context.isFailed()) {
                logger.error("Cannot execute the plugin {}", plugin.getClass());
                return;
            }
            try {
                if (before) {
                    plugin.doBefore(context);
                } else {
                    plugin.doAfter(context);
                }
            } catch (HyscaleException he) {
                context.setHyscaleException(he);
                plugin.onError(context, he);
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
