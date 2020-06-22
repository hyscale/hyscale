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
package io.hyscale.deployer.services.handler;

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
	public static List<ResourceLifeCycleHandler> getAllHandlers(){
	    if (kindVsHandler == null) {
		return null;
	    }
	    return Collections.unmodifiableList(kindVsHandler.values().stream().collect(Collectors.toList()));
	}
	
}
