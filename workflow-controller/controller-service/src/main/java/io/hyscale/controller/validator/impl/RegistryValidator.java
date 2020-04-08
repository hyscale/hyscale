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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.ImageRegistry;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.activity.ValidatorActivity;
import io.hyscale.controller.manager.RegistryManager;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.ImageDetailsUtil;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;

@Component
public class RegistryValidator implements Validator<WorkflowContext> {
	private static final Logger logger = LoggerFactory.getLogger(ClusterValidator.class);

	@Autowired
	private RegistryManager registryManager;
	
	private Set<String> registries=new HashSet<String>();

	@Override
	public boolean validate(WorkflowContext context) throws HyscaleException {
		logger.debug("Starting registry validation");
		if (!ImageDetailsUtil.isImageBuildPushRequired(context.getServiceSpec())) {
			return true;
		}
		ImageRegistry imageRegistry = context.getServiceSpec().get(
				HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), ImageRegistry.class);
		boolean isRegistryAvailable = registries.contains(imageRegistry.getName());
		if (isRegistryAvailable) {
			return true;
		} else {
			isRegistryAvailable = registryManager.getImageRegistry(imageRegistry.getName()) != null ? true : false;
		}
		if (isRegistryAvailable) {
			registries.add(imageRegistry.getName());
			return true;
		} else {
			WorkflowLogger.persistError(ValidatorActivity.REGISTRY_VALIDATION, "Registry validation failed");
			return false;
		}
	}
}
