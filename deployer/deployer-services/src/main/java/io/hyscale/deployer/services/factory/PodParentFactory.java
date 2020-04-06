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

import io.hyscale.deployer.services.handler.PodParentHandler;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* TODO
  need to inject the parentHandlers
  to parenthandler list. Until then , this
  class is incomplete
 */
public class PodParentFactory {

    private List<PodParentHandler> parentHandlerList;

    Map<String, PodParentHandler> kindVsHandlerMap;

    @PostConstruct
    public void init() {
        kindVsHandlerMap = new HashMap<>();
        parentHandlerList.forEach(each -> {
            kindVsHandlerMap.put(each.getKind(), each);
        });
    }

    public PodParentHandler getHandler(String kind) {
        return kindVsHandlerMap.get(kind);
    }
}
