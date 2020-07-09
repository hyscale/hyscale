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
package io.hyscale.generator.services.listener;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.generator.services.utils.PluginHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ManifestStartupListener {

	@Autowired
	private PluginHandlers pluginHandlers;

	private static final Logger logger = LoggerFactory.getLogger(ManifestStartupListener.class);

	@EventListener
	public void onApplicationEvent(Object event) throws HyscaleException {
		if (event instanceof ContextRefreshedEvent) {
			logger.debug("Registering Manifest handlers");
			pluginHandlers.registerHandlers();
		}
	}
}
