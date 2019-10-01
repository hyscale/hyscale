package io.hyscale.ctl.deployer.services.listener;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.deployer.services.handler.ResourceHandlers;

@Component
public class DeployerStartUpListener {

	@EventListener
	public void onApplicationEvent(Object event) {
		if (event instanceof ContextRefreshedEvent) {
			ResourceHandlers.registerHandlers();
		}
	}

}
