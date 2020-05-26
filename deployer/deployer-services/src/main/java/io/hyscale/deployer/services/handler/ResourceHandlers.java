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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;

@Component
public class ResourceHandlers {

	private Map<String, ResourceLifeCycleHandler> kindVsHandler;
	
	@Autowired
	private List<ResourceLifeCycleHandler> resourceLifeCycleHandlerBeans;
	
	private static final String RESOURCE_HANDLERS_LIST = "config/resourceHandlers.txt";

	@PostConstruct
	public void registerHandlers() throws HyscaleException {
		if (kindVsHandler == null) {
			kindVsHandler = new HashMap();
			InputStream is = ResourceHandlers.class.getClassLoader().getResourceAsStream(RESOURCE_HANDLERS_LIST);
            try {
                List<String> resourceHandlersList = IOUtils.readLines(is, ToolConstants.CHARACTER_ENCODING);

                Map<String, ResourceLifeCycleHandler> classVsHandlerMap = resourceLifeCycleHandlerBeans.stream()
                        .collect(Collectors.toMap(key -> key.getClass().getName(), value -> value));

                resourceHandlersList.stream().forEach(each -> {
                    ResourceLifeCycleHandler resourceHandler = classVsHandlerMap.get(each);
                    kindVsHandler.put(resourceHandler.getKind(), resourceHandler);
                });

            } catch (IOException e) {
                HyscaleException ex = new HyscaleException(DeployerErrorCodes.FAILED_TO_CONFIGURE_HANDLERS);
                throw ex;
            }
		}
	}
	
	/**
	 * 
	 * @param kind
	 * @return ResourceLifeCycleHandler for the kind, null if not found
	 */
	public ResourceLifeCycleHandler getHandlerOf(String kind) {
		return kindVsHandler.get(kind);
	}
	
	public <T extends ResourceLifeCycleHandler> T getHandlerOf(String kind, Class<T> klazz) {
	    if (kindVsHandler.containsKey(kind)) {
	        return (T) kindVsHandler.get(kind);
	    }
	    return null;
    }
	
	/**
	 * 
	 * @return Unmodifiable list of all available ResourceLifeCycleHandler
	 */
	public List<ResourceLifeCycleHandler> getAllHandlers(){
	    if (kindVsHandler == null) {
		return null;
	    }
	    return Collections.unmodifiableList(kindVsHandler.values().stream().collect(Collectors.toList()));
	}
	
}
