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
package io.hyscale.dockerfile.gen.services.generator.impl;

import java.util.HashMap;
import java.util.Map;

import io.hyscale.commons.models.DecoratedArrayList;
import io.hyscale.dockerfile.gen.services.generator.DockerfileContentGenerator;
import io.hyscale.dockerfile.gen.services.templates.DockerfileTemplateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.dockerfile.gen.services.constants.DockerfileGenConstants;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.core.models.CommandType;
import io.hyscale.dockerfile.gen.core.models.DockerfileContent;
import io.hyscale.dockerfile.gen.services.exception.DockerfileErrorCodes;
import io.hyscale.dockerfile.gen.core.models.ImageType;
import io.hyscale.dockerfile.gen.services.config.DockerfileGenConfig;
import io.hyscale.dockerfile.gen.services.manager.impl.DockerScriptManagerImpl;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.springframework.util.CollectionUtils;

@Component
public class DockerfileContentGenImpl implements DockerfileContentGenerator {

	private static final String PERMISSION_COMMAND = "chmod -R 755";

	@Autowired
	private DockerfileTemplateProvider templateProvider;

	@Autowired
	private MustacheTemplateResolver templateResolver;

	@Autowired
	private DockerfileGenConfig dockerfileGenConfig;

	@Autowired
	private DockerScriptManagerImpl dockerScriptHelper;

	@Override
	public DockerfileContent generate(ServiceSpec serviceSpec, DockerfileGenContext context) throws HyscaleException {
		BuildSpec buildSpec = serviceSpec
				.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec), BuildSpec.class);
		if (buildSpec == null) {
			throw new HyscaleException(DockerfileErrorCodes.BUILD_SPEC_REQUIRED);
		}
		// TODO other image type
		ConfigTemplate configTemplate = templateProvider.getTemplateFor(ImageType.ARTIFACT);
		Map<String, Object> dockerfileContext = new HashMap<String, Object>();
		dockerfileContext.put(DockerfileGenConstants.STACK_IMAGE, buildSpec.getStackImage());

		DecoratedArrayList effectiveArtifacts = new DecoratedArrayList();
		if (!CollectionUtils.isEmpty(context.getEffectiveArtifacts())) {
			effectiveArtifacts.addAll(context.getEffectiveArtifacts());
		}

		dockerfileContext.put(DockerfileGenConstants.ARTIFACTS, effectiveArtifacts);
		dockerfileContext.put(DockerfileGenConstants.SCRIPT_DIR_FIELD, dockerfileGenConfig.getScriptDestinationDir());

		boolean configScriptAvailable = dockerScriptHelper.scriptAvailable(buildSpec.getConfigCommands(),
				buildSpec.getConfigCommandsScript());
		if (configScriptAvailable) {
			String configScript = dockerScriptHelper.getScriptFile(buildSpec.getConfigCommandsScript(), CommandType.CONFIGURE);
			dockerfileContext.put(DockerfileGenConstants.CONFIGURE_SCRIPT_FILE_FIELD, configScript);

		}
		boolean runScriptAvailable = dockerScriptHelper.scriptAvailable(buildSpec.getRunCommands(),
				buildSpec.getRunCommandsScript());
		if (runScriptAvailable) {
			String runScript = dockerScriptHelper.getScriptFile(buildSpec.getRunCommandsScript(), CommandType.RUN);
			dockerfileContext.put(DockerfileGenConstants.RUN_SCRIPT_FILE_FIELD, runScript);
			dockerfileContext.put(DockerfileGenConstants.SHELL_START_FIELD, dockerfileGenConfig.getShellStartScript());
		}
		if (configScriptAvailable || runScriptAvailable) {
			dockerfileContext.put(DockerfileGenConstants.PERMISSION_COMMAND_FIELD, PERMISSION_COMMAND);
		}
		String content = templateResolver.resolveTemplate(configTemplate.getTemplatePath(), dockerfileContext);

		DockerfileContent dockerfileContent = new DockerfileContent();
		dockerfileContent.setContent(content);

		return dockerfileContent;
	}

}
