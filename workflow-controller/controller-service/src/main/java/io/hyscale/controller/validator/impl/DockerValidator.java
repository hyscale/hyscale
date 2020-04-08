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

import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.RegistryAndDockerValidatorUtil;

@Component
public class DockerValidator implements Validator<WorkflowContext> {
	private static final Logger logger = LoggerFactory.getLogger(DockerValidator.class);

	@Autowired
	private ImageCommandProvider commandGenerator;

	@Override
	public boolean validate(WorkflowContext context) throws HyscaleException {
		if(!RegistryAndDockerValidatorUtil.isValidate(context.getServiceSpec())) {
			return false;
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
			WorkflowLogger.error(ImageBuilderActivity.DOCKER_DAEMON_NOT_RUNNING);
			return false;
		}
		return true;
	}
}
