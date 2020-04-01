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
package io.hyscale.controller.processors;

import io.hyscale.commons.component.PrePostProcessors;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Hook to clean service directory to remove files no longer required
 *
 */
@Component
public class ServiceDirCleanUpProcessor implements PrePostProcessors<WorkflowContext> {

	private static final Logger logger = LoggerFactory.getLogger(ServiceDirCleanUpProcessor.class);

	@Autowired
	private SetupConfig setupConfig;

	@Override
	public void preProcess(WorkflowContext context) throws HyscaleException {
		if (context.getServiceName() != null && context.getAttribute(WorkflowConstants.CLEAN_UP_SERVICE_DIR) != null
				&& context.getAttribute(WorkflowConstants.CLEAN_UP_SERVICE_DIR).equals(true)) {
			String serviceDir = setupConfig.getServiceDir(context.getAppName(), context.getServiceName());
			HyscaleFilesUtil.deleteDirectory(serviceDir);
			logger.debug("Cleaning up service dir in the apps");
		}
	}

	@Override
	public void postProcess(WorkflowContext context) throws HyscaleException {

	}

	@Override
	public void onError(WorkflowContext context, Throwable th) {

	}
}
