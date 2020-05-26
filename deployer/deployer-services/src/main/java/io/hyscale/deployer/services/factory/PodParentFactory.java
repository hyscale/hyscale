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
package io.hyscale.deployer.services.factory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.deployer.services.handler.PodParentHandler;

/**
 * Factory Class to provide PodParentHandlers based on Kind
 *
 */
@Component
public class PodParentFactory {

    private Map<String, PodParentHandler> kindVsHandlerMap;

    @Autowired
    private List<PodParentHandler> podParentHandlerList;

    @PostConstruct
    public void registerHandlers() {
        if (kindVsHandlerMap == null) {
            kindVsHandlerMap = new HashMap();
            for (PodParentHandler handler : podParentHandlerList) {
                kindVsHandlerMap.put(handler.getKind(), handler);
            }
        }
    }

    public PodParentHandler getHandlerOf(String kind) {
        return kindVsHandlerMap.get(kind);
    }
    
    /**
     * 
     * @return Unmodifiable list of all available PodParentHandlers
     */
    public List<PodParentHandler> getAllHandlers(){
        if (kindVsHandlerMap == null) {
        return null;
        }
        return Collections.unmodifiableList(kindVsHandlerMap.values().stream().collect(Collectors.toList()));
    }

}
