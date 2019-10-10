package io.hyscale.ctl.dockerfile.gen.services.generator.impl;

import java.util.HashMap;
import java.util.Map;

import io.hyscale.ctl.commons.models.DecoratedArrayList;
import io.hyscale.ctl.dockerfile.gen.services.generator.DockerfileContentGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.servicespec.commons.model.service.BuildSpec;
import io.hyscale.ctl.commons.models.ConfigTemplate;
import io.hyscale.ctl.commons.utils.MustacheTemplateResolver;
import io.hyscale.ctl.dockerfile.gen.services.constants.DockerfileGenConstants;
import io.hyscale.ctl.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.ctl.dockerfile.gen.core.models.CommandType;
import io.hyscale.ctl.dockerfile.gen.core.models.DockerfileContent;
import io.hyscale.ctl.dockerfile.gen.services.exception.DockerfileErrorCodes;
import io.hyscale.ctl.dockerfile.gen.core.models.ImageType;
import io.hyscale.ctl.dockerfile.gen.services.config.DockerfileGenConfig;
import io.hyscale.ctl.dockerfile.gen.services.templates.DockerfileTemplateProvider;
import io.hyscale.ctl.dockerfile.gen.services.manager.impl.DockerScriptManagerImpl;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
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
				buildSpec.getConfigScript());
		if (configScriptAvailable) {
			String configScript = dockerScriptHelper.getScriptFile(buildSpec.getConfigScript(), CommandType.CONFIGURE);
			dockerfileContext.put(DockerfileGenConstants.CONFIGURE_SCRIPT_FILE_FIELD, configScript);

		}
		boolean runScriptAvailable = dockerScriptHelper.scriptAvailable(buildSpec.getRunCommands(),
				buildSpec.getRunScript());
		if (runScriptAvailable) {
			String runScript = dockerScriptHelper.getScriptFile(buildSpec.getRunScript(), CommandType.RUN);
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
