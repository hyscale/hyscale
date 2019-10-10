package io.hyscale.ctl.dockerfile.gen.services.templates;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import io.hyscale.ctl.commons.models.ConfigTemplate;
import io.hyscale.ctl.dockerfile.gen.core.models.CommandType;

@Component
public class CommandsTemplateProvider {

	private Map<CommandType, ConfigTemplate> commandVsTemplateMap;

	protected static final String TEMPLATES_PATH = "templates/";
	private static final String CONFIGURE_COMMAND_TPL = "configure-cmd-script.tpl";
	private static final String RUN_COMMAND_TPL = "run-cmd-script.tpl";

	@PostConstruct
	public void init() {
		this.commandVsTemplateMap = Maps.newHashMap();

		ConfigTemplate configureCmdTpl = new ConfigTemplate();
		configureCmdTpl.setRootPath(TEMPLATES_PATH);
		configureCmdTpl.setTemplateName(CONFIGURE_COMMAND_TPL);

		commandVsTemplateMap.put(CommandType.CONFIGURE, configureCmdTpl);

		ConfigTemplate runCmdTpl = new ConfigTemplate();
		runCmdTpl.setRootPath(TEMPLATES_PATH);
		runCmdTpl.setTemplateName(RUN_COMMAND_TPL);

		commandVsTemplateMap.put(CommandType.RUN, runCmdTpl);

	}

	public ConfigTemplate getTemplateFor(CommandType commandType) {
		return commandVsTemplateMap.get(commandType);
	}
}
