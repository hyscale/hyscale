package io.hyscale.dockerfile.gen.services.manager.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.dockerfile.gen.services.manager.DockerfileEntityManager;
import io.hyscale.dockerfile.gen.services.constants.DockerfileGenConstants;
import io.hyscale.dockerfile.gen.services.templates.CommandsTemplateProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.core.models.CommandType;
import io.hyscale.commons.models.FileSpec;
import io.hyscale.commons.models.SupportingFile;
import io.hyscale.dockerfile.gen.services.config.DockerfileGenConfig;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class DockerScriptManagerImpl implements DockerfileEntityManager {

	@Autowired
	private CommandsTemplateProvider templateProvider;

	@Autowired
	private MustacheTemplateResolver templateResolver;

	/**
	 * If Script provided copy it to the directory Config Script, Run Script, Init
	 * Script In case of commands write it to script file
	 */
	@Override
	public List<SupportingFile> getSupportingFiles(ServiceSpec serviceSpec, DockerfileGenContext context)
			throws HyscaleException {
		if (serviceSpec == null) {
			throw new HyscaleException(CommonErrorCode.SERVICE_SPEC_REQUIRED);
		}
		List<SupportingFile> supportingFiles = new ArrayList<SupportingFile>();

		BuildSpec buildSpec = serviceSpec
				.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec), BuildSpec.class);

		boolean configCmdAvailable = scriptAvailable(buildSpec.getConfigCommands(), buildSpec.getConfigCommandsScript());
		SupportingFile configFile = null;
		if (configCmdAvailable) {
			configFile = getCommandSupportFile(buildSpec.getConfigCommands(), buildSpec.getConfigCommandsScript(),
					CommandType.CONFIGURE);
			if (configFile != null) {
				supportingFiles.add(configFile);
			}
		}
		boolean runCmdAvailable = scriptAvailable(buildSpec.getRunCommands(), buildSpec.getRunCommandsScript());
		SupportingFile runCmdFile = null;
		if (runCmdAvailable) {
			runCmdFile = getCommandSupportFile(buildSpec.getRunCommands(), buildSpec.getRunCommandsScript(), CommandType.RUN);
			if (runCmdFile != null) {
				supportingFiles.add(runCmdFile);
			}
		}

		return supportingFiles;
	}

	private SupportingFile getCommandSupportFile(String commands, String script, CommandType commandType)
			throws HyscaleException {

		FileSpec fileSpec = new FileSpec();

		SupportingFile supportingFile = new SupportingFile();
		switch (commandType) {
		case CONFIGURE:
			if (StringUtils.isNotBlank(script)) {
				supportingFile.setFile(new File(SetupConfig.getAbsolutePath(script)));

				return supportingFile;
			} else {
				fileSpec.setContent(getConfigureCmdScript(commands));
				fileSpec.setName(DockerfileGenConfig.CONFIGURE_SCRIPT);
			}
			break;
		case RUN:
			if (StringUtils.isNotBlank(script)) {
				supportingFile.setFile(new File(SetupConfig.getAbsolutePath(script)));
				return supportingFile;
			} else {
				fileSpec.setContent(getRunCmdScript(commands));
				fileSpec.setName(DockerfileGenConfig.RUN_SCRIPT);
			}
			break;
		default:
			break;

		}
		if (StringUtils.isBlank(fileSpec.getContent())) {
			return null;
		}
		supportingFile.setFileSpec(fileSpec);

		return supportingFile;
	}

	public String getRunCmdScript(String runCommand) throws HyscaleException {
		if (StringUtils.isBlank(runCommand)) {
			return null;
		}
		ConfigTemplate configTemplate = templateProvider.getTemplateFor(CommandType.RUN);
		Map<String, Object> runCmdContext = new HashMap<String, Object>();

		if (StringUtils.isNotBlank(runCommand)) {
			runCmdContext.put(DockerfileGenConstants.RUN_COMMANDS_FIELD, runCommand);
		}
		return templateResolver.resolveTemplate(configTemplate.getTemplatePath(), runCmdContext);
	}

	public String getConfigureCmdScript(String configCommand) throws HyscaleException {
		if (StringUtils.isBlank(configCommand)) {
			return null;
		}
		ConfigTemplate configTemplate = templateProvider.getTemplateFor(CommandType.CONFIGURE);
		Map<String, Object> configureCmdContext = new HashMap<String, Object>();
		configureCmdContext.put(DockerfileGenConstants.CONFIGURE_COMMANDS_FIELD, configCommand);

		return templateResolver.resolveTemplate(configTemplate.getTemplatePath(), configureCmdContext);

	}

	public String getScriptFile(String scriptFile, CommandType commandType) {
		if (StringUtils.isNotBlank(scriptFile)) {
			return new File(scriptFile).getName();
		}
		switch (commandType) {
		case CONFIGURE:
			return DockerfileGenConfig.CONFIGURE_SCRIPT;
		case RUN:
			return DockerfileGenConfig.RUN_SCRIPT;
		default:
			break;

		}
		return null;
	}

	public boolean scriptAvailable(String commands, String script) {
		if (StringUtils.isBlank(script) && StringUtils.isBlank(commands)) {
			return false;
		}
		return true;
	}

}
