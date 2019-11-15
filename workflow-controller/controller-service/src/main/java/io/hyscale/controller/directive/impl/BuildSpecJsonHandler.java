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
package io.hyscale.controller.directive.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.directive.ServiceSpecDirectiveUpdateHandler;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Artifact;
import io.hyscale.servicespec.commons.model.service.BuildSpec;

/**
 * Updates the "build spec" directive from the service spec before processing the
 * service spec for deployment. Update operation is like a pre-processor and
 * does not change the state of the service spec
 * 
 * @author tushart
 *
 */
@Component
public class BuildSpecJsonHandler extends ServiceSpecDirectiveUpdateHandler<BuildSpec> {

	private static final Logger logger = LoggerFactory.getLogger(BuildSpecJsonHandler.class);

	@Override
	public BuildSpec updateObject(BuildSpec buildSpec) {
		// Artifacts
		List<Artifact> artifacts = buildSpec.getArtifacts();
		if (artifacts != null) {
			artifacts.forEach(artifact -> {
				artifact.setSource(WindowsUtil.updateToUnixFileSeparator(artifact.getSource()));
			});
			buildSpec.setArtifacts(artifacts);
		}

		// Config Commands Script
		String configCmdScript = buildSpec.getConfigCommandsScript();
		if (StringUtils.isNotBlank(configCmdScript)) {
			buildSpec.setConfigCommandsScript(WindowsUtil.updateToUnixFileSeparator(configCmdScript));
		}

		// Run Commands Script
		String runCmdScripts = buildSpec.getRunCommandsScript();
		if (StringUtils.isNotBlank(runCmdScripts)) {
			buildSpec.setRunCommandsScript(WindowsUtil.updateToUnixFileSeparator(runCmdScripts));
		}
		return buildSpec;
	}

	@Override
	protected String[] getPath() {
		return new String[] { HyscaleSpecFields.image, HyscaleSpecFields.buildSpec };
	}

	@Override
	protected Class<BuildSpec> getType() {
		return BuildSpec.class;
	}

}
