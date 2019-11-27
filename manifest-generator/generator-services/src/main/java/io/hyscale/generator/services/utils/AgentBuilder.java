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
package io.hyscale.generator.services.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

import java.util.List;

public interface AgentBuilder {

    public List<ManifestSnippet> build(ManifestContext manifestContext, ServiceSpec serviceSpec) throws JsonProcessingException;

    default List<Agent> getAgents(ServiceSpec serviceSpec){
        TypeReference<List<Agent>> agentsList = new TypeReference<List<Agent>>() {
        };
        try {
            List<Agent> agents = serviceSpec.get(HyscaleSpecFields.agents, agentsList);
            return  agents;
        } catch (HyscaleException e) {
            return null;
        }
    }

}
