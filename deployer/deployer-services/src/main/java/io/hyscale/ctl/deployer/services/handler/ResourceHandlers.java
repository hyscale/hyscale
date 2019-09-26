package io.hyscale.ctl.deployer.services.handler;

import java.util.*;
import java.util.stream.Collectors;

public final class ResourceHandlers {

	private static Map<String, ResourceLifeCycleHandler> kindVsHandler;

	public static void registerHandlers() {
		if (kindVsHandler == null) {
			kindVsHandler = new HashMap();
			for (ResourceLifeCycleHandler handler : ServiceLoader.load(ResourceLifeCycleHandler.class,
					ResourceHandlers.class.getClassLoader())) {
				kindVsHandler.put(handler.getKind(), handler);
			}
		}
	}

	public static ResourceLifeCycleHandler getHandlerOf(String kind) {
		return kindVsHandler.get(kind);
	}
	
	public static List<ResourceLifeCycleHandler> getHandlersList(){
	    if (kindVsHandler == null) {
		return null;
	    }
	    return Collections.unmodifiableList(kindVsHandler.values().stream().collect(Collectors.toList()));
	}
	
}
