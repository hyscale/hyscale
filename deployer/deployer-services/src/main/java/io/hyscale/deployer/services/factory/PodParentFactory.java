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

import java.util.*;

import javax.annotation.PostConstruct;

import io.hyscale.deployer.services.handler.PodParentHandler;

/* TODO
  need to inject the parentHandlers
  to parenthandler list. Until then , this
  class is incomplete
 */
public class PodParentFactory {

    private static Map<String, PodParentHandler> kindVsHandlerMap;


    public static void registerHandlers() {
        if (kindVsHandlerMap == null) {
            kindVsHandlerMap = new HashMap();
            for (PodParentHandler handler : ServiceLoader.load(PodParentHandler.class, PodParentFactory.class.getClassLoader())) {
                kindVsHandlerMap.put(handler.getKind(), handler);
            }
        }
    }

    public static PodParentHandler getHandler(String kind) {
        return kindVsHandlerMap.get(kind);
    }

}
