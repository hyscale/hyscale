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
package io.hyscale.dockerfile.gen.services.templates;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.dockerfile.gen.services.model.CommandType;

@Component
public class CommandsTemplateProvider {

	private Map<CommandType, ConfigTemplate> commandVsTemplateMap;

	protected static final String TEMPLATES_PATH = "templates";
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
