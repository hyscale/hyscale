package io.hyscale.ctl.commons.utils;

import java.util.concurrent.*;

public class ThreadPoolUtil {

	private static final int CORE_POOL_SIZE = 10;
	private static final int MAX_POOL_SIZE = 150;
	private static final int BLOCKING_QUEUE_SIZE = 5;
	private static final int MAX_FIRING_RETRIES = 3;

	private ThreadPoolExecutor executor;

	private static final class InstanceHolder {
		private static final ThreadPoolUtil INSTANCE = new ThreadPoolUtil();
	}

	public static ThreadPoolUtil getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private ThreadPoolUtil() {
		executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 60 * 1000, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue(BLOCKING_QUEUE_SIZE));
	}

	public void executeWithRetries(Runnable runnable) {
		boolean fired = false;
		int retries = 0;
		while (!fired && ++retries < MAX_FIRING_RETRIES + 1) {
			fired = execute(runnable);
			sleepSilently(1000L);
		}
	}

	public boolean execute(Runnable runnable) {
		try {
			executor.execute(runnable);
			return true;
		} catch (RejectedExecutionException re) {

		}
		return false;
	}

	public <T> Future<T> execute(Callable<T> callable) {
		try {
			return executor.submit(callable);
		} catch (RejectedExecutionException re) {

		}
		return null;
	}

	public static void sleepSilently(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			//
		}
	}

	public void shutdown() {
		try {
			executor.shutdown();
		} catch (Exception e) {

		}
	}
}
