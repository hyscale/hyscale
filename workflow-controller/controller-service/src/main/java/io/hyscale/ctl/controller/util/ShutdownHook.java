package io.hyscale.ctl.controller.util;

import io.hyscale.ctl.commons.utils.ThreadPoolUtil;

/**
 * <p>
 * This class runs just before termination of program when
 * registered with Java Runtime.
 * Responsible for cleaning stale resources,
 * stop any running executors etc
 * </p>
 */
public class ShutdownHook extends Thread {

    @Override
    public void run() {
        ThreadPoolUtil.getInstance().shutdown();
    }
}
