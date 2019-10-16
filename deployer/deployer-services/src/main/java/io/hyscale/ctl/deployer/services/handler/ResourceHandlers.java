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

	/**
	 * 
	 * @param kind
	 * @return ResourceLifeCycleHandler for the kind, null if not found
	 */
	public static ResourceLifeCycleHandler getHandlerOf(String kind) {
		return kindVsHandler.get(kind);
	}
	
	/**
	 * 
	 * @return Unmodifiable list of all available ResourceLifeCycleHandler
	 */
	public static List<ResourceLifeCycleHandler> getHandlersList(){
	    if (kindVsHandler == null) {
		return null;
	    }
	    return Collections.unmodifiableList(kindVsHandler.values().stream().collect(Collectors.toList()));
	}
	
}
