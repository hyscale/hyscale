package io.hyscale.ctl.generator.services.listener;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.generator.services.utils.PluginHandlers;
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
			logger.debug("Registring Manifest handlers {}");
			pluginHandlers.registerHandlers();
		}
	}
}
