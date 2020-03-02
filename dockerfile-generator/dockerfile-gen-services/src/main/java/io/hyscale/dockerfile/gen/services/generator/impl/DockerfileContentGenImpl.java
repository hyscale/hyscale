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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.commons.models.DecoratedArrayList;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.dockerfile.gen.services.model.CommandType;
import io.hyscale.dockerfile.gen.core.models.DockerfileContent;
import io.hyscale.dockerfile.gen.services.config.DockerfileGenConfig;
import io.hyscale.dockerfile.gen.services.constants.DockerfileGenConstants;
import io.hyscale.dockerfile.gen.services.exception.DockerfileErrorCodes;
import io.hyscale.dockerfile.gen.services.generator.DockerfileContentGenerator;
import io.hyscale.dockerfile.gen.services.manager.impl.DockerScriptManagerImpl;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.services.templates.DockerfileTemplateProvider;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class DockerfileContentGenImpl implements DockerfileContentGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(DockerfileContentGenImpl.class);

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
		Map<String, Object> dockerfileContext = new HashMap<String, Object>();
		dockerfileContext.put(DockerfileGenConstants.STACK_IMAGE, buildSpec.getStackImage());

		DecoratedArrayList effectiveArtifacts = new DecoratedArrayList();
		if (!CollectionUtils.isEmpty(context.getEffectiveArtifacts())) {
			effectiveArtifacts.addAll(context.getEffectiveArtifacts());
		}

		dockerfileContext.put(DockerfileGenConstants.ARTIFACTS, effectiveArtifacts);
		String scriptDestinationDir = dockerfileGenConfig.getScriptDestinationDir();
		dockerfileContext.put(DockerfileGenConstants.SCRIPT_DIR_FIELD, scriptDestinationDir);

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
		    StringBuilder command = new StringBuilder();
		    if (WindowsUtil.isHostWindows()) {
		        command.append(getUpdateScriptCommand(dockerfileContext, scriptDestinationDir));
		    }
		    command.append(DockerfileGenConstants.PERMISSION_COMMAND).append(ToolConstants.SPACE).append(scriptDestinationDir);
			dockerfileContext.put(DockerfileGenConstants.PERMISSION_COMMAND_FIELD, command);
		}
		// TODO other image type
		ConfigTemplate configTemplate = templateProvider.getDockerfileTemplate();
		String content = templateResolver.resolveTemplate(configTemplate.getTemplatePath(), dockerfileContext);

		DockerfileContent dockerfileContent = new DockerfileContent();
		dockerfileContent.setContent(content);

		return dockerfileContent;
	}
	
	/**
	 * Building an image with a script created in windows does not work due to LF-styles in windows i.e. (\r\n)
	 * whereas in linux it is (“\n”)
	 * This method provides the command to update scripts to work with linux containers
	 * @param dockerfileContext
	 * @param scriptDestinationDir
	 * @return script update command
	 */
	private String getUpdateScriptCommand(Map<String, Object> dockerfileContext, String scriptDestinationDir) {
	    StringBuilder command = new StringBuilder();
	    logger.debug("Updating script files as OS is Windows");
        Object runScript = dockerfileContext.get(DockerfileGenConstants.RUN_SCRIPT_FILE_FIELD);
        if (runScript != null) {
            command.append(dockerScriptHelper.getScriptUpdateCommand(scriptDestinationDir + runScript.toString()));
            command.append(ToolConstants.SPACE).append(ToolConstants.COMMAND_SEPARATOR).append(ToolConstants.SPACE);
        }
        Object configScript = dockerfileContext.get(DockerfileGenConstants.CONFIGURE_SCRIPT_FILE_FIELD);
        if (configScript != null) {
            command.append(dockerScriptHelper.getScriptUpdateCommand(scriptDestinationDir + configScript.toString()));
            command.append(ToolConstants.SPACE).append(ToolConstants.COMMAND_SEPARATOR).append(ToolConstants.SPACE);
        }
        return command.toString();
	}
	
}
