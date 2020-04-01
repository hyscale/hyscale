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
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.generator.services.config.ManifestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Hook to clean up old manifests
 *
 */
@Component
public class ManifestCleanUpProcessor implements PrePostProcessors<WorkflowContext> {

	private static final Logger logger = LoggerFactory.getLogger(ManifestCleanUpProcessor.class);

	@Autowired
	private ManifestConfig manifestConfig;

	@Override
	public void preProcess(WorkflowContext context) throws HyscaleException {
		String manifestDir = manifestConfig.getManifestDir(context.getAppName(), context.getServiceName());
		String absManifestDir = SetupConfig.getMountPathOf(manifestDir);
		logger.debug("Cleaning up manifests directory {}", absManifestDir);
		HyscaleFilesUtil.clearDirectory(absManifestDir);
		logger.debug("Manifest directory cleaned");
	}

	@Override
	public void postProcess(WorkflowContext context) throws HyscaleException {

	}

	@Override
	public void onError(WorkflowContext context, Throwable th) {
	    logger.error("Error while clearing manifests directory, ignoring", th);
	}
}
