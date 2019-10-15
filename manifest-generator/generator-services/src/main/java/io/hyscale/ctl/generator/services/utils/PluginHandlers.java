package io.hyscale.ctl.generator.services.utils;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.generator.services.exception.ManifestErrorCodes;
import io.hyscale.ctl.plugin.framework.handler.ManifestHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PluginHandlers {

	private static final Logger logger = LoggerFactory.getLogger(PluginHandlers.class);

	@Autowired
	private List<ManifestHandler> manifestHandlerBeans;

	private List<ManifestHandler> manifestHandlers;

	public void registerHandlers() throws HyscaleException {
		if (manifestHandlerBeans != null) {
			manifestHandlers = new ArrayList<>();
			InputStream is = PluginHandlers.class.getClassLoader().getResourceAsStream("config/plugins.txt");
			try {
				List<String> pluginsList = IOUtils.readLines(is);

				Map<String, ManifestHandler> classVsHandlerMap = manifestHandlerBeans.stream()
						.collect(Collectors.toMap(key -> key.getClass().getName(), value -> value));

				pluginsList.stream().forEach(each -> {
					manifestHandlers.add(classVsHandlerMap.get(each));
				});

			} catch (IOException e) {
				HyscaleException ex = new HyscaleException(ManifestErrorCodes.ERROR_WHILE_CREATING_MANIFEST);
				throw ex;
			}
		}
	}

	public List<ManifestHandler> getAllPlugins() {
		return Collections.unmodifiableList(manifestHandlers);
	}
}
