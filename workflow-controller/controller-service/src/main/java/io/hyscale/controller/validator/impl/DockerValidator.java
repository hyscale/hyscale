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
package io.hyscale.controller.validator.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.activity.ValidatorActivity;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.servicespec.commons.util.ImageUtil;

@Component
public class DockerValidator implements Validator<WorkflowContext> {
	private static final Logger logger = LoggerFactory.getLogger(DockerValidator.class);

	@Autowired
	private ImageCommandProvider commandGenerator;

	private boolean isDockerAvailable = false;;

	/**
	 * 1. It will check that spec has buildspec or dockerfile 
	 * 2. If both is not then it will return true
	 * 3. If any one is there then 
	 *    3.1  It will verify that docker is installed or not
	 *    3.2  It will run docker command 
	 *    3.3  if command executed successfully then return true else false
	 */
	@Override
	public boolean validate(WorkflowContext context) throws HyscaleException {
		if (!ImageUtil.isImageBuildPushRequired(context.getServiceSpec())) {
			return true;
		}
		if (isDockerAvailable) {
			return isDockerAvailable;
		}
		String command = commandGenerator.dockerVersion();
		logger.debug("Docker Installed check command: {}", command);
		boolean success = CommandExecutor.execute(command);
		if (!success) {
			return false;
		}
		command = commandGenerator.dockerImages();
		logger.debug("Docker Daemon running check command: {}", command);
		success = CommandExecutor.execute(command);
		if (!success) {
			WorkflowLogger.error(ValidatorActivity.DOCKER_VALIDATION,"Docker validation failed");
			return false;
		}
		isDockerAvailable = true;
		return isDockerAvailable;
	}
}
