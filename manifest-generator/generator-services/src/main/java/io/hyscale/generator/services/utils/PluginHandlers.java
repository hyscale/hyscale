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
package io.hyscale.generator.services.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.plugin.framework.handler.ManifestHandler;

@Component
public class PluginHandlers {

	private static final Logger logger = LoggerFactory.getLogger(PluginHandlers.class);

	@Autowired
	private List<ManifestHandler> manifestHandlerBeans;

	private List<ManifestHandler> manifestHandlers;
	
	private static final String PLUGINS_LIST = "config/plugins.txt";

	public void registerHandlers() throws HyscaleException {
	    if (manifestHandlerBeans == null) {
	        return;
	    }
	    manifestHandlers = new ArrayList<>();
	    InputStream is = PluginHandlers.class.getClassLoader().getResourceAsStream(PLUGINS_LIST);
	    try {
	        List<String> pluginsList = IOUtils.readLines(is, ToolConstants.CHARACTER_ENCODING);
	        
	        Map<String, ManifestHandler> classVsHandlerMap = manifestHandlerBeans.stream()
	                .collect(Collectors.toMap(key -> key.getClass().getName(), value -> value));
	        
	        pluginsList.stream().forEach(each -> {
	            manifestHandlers.add(classVsHandlerMap.get(each));
	        });
	        
	    } catch (IOException e) {
	        logger.error("Error while registering manifest handlers", e);
	        HyscaleException ex = new HyscaleException(ManifestErrorCodes.ERROR_WHILE_REGISTERING_HANDLERS);
	        throw ex;
	    }
	}

	public List<ManifestHandler> getAllPlugins() {
		return Collections.unmodifiableList(manifestHandlers);
	}
}
