package io.hyscale.deployer.services.listener;

import io.hyscale.deployer.services.handler.ResourceHandlers;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DeployerStartUpListener {

	/**
	 * Registers Cluster resource handlers
	 * @param event
	 */
	@EventListener
	public void onApplicationEvent(Object event) {
		if (event instanceof ContextRefreshedEvent) {
			ResourceHandlers.registerHandlers();
		}
	}

}
