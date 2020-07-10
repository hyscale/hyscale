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
package io.hyscale.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class ThreadPoolUtil {

	private static final int CORE_POOL_SIZE = 10;
	private static final int MAX_POOL_SIZE = 150;
	private static final int BLOCKING_QUEUE_SIZE = 5;
	private static final int MAX_FIRING_RETRIES = 3;

	private ThreadPoolExecutor executor;

	private static final Logger logger = LoggerFactory.getLogger(ThreadPoolUtil.class);

	private static final class InstanceHolder {
		private static final ThreadPoolUtil INSTANCE = new ThreadPoolUtil();
	}

	public static ThreadPoolUtil getInstance() {
		return InstanceHolder.INSTANCE;
	}

	private ThreadPoolUtil() {
		executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, (long) 60 * 1000, TimeUnit.MILLISECONDS,
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
			logger.error("Error while executing thread.",re);
		}
		return false;
	}

	public <T> Future<T> execute(Callable<T> callable) {
		try {
			return executor.submit(callable);
		} catch (RejectedExecutionException re) {
			logger.error("Error while submitting thread to executor",re);
		}
		return null;
	}

	public static void sleepSilently(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void shutdown() {
		try {
			executor.shutdown();
		} catch (Exception e) {
			logger.error("Error while performing executor shutdown",e);
		}
	}
}
