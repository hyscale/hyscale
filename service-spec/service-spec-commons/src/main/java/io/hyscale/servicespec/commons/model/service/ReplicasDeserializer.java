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
package io.hyscale.servicespec.commons.model.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.servicespec.commons.activity.ServiceSpecActivity;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;

import java.io.IOException;

/**
 *  Deserializer for replicas field in the service spec.
 *
 *  replicas:
 *      min: {min replicas}
 *      max: {max replicas}
 *      cpuThreshold: {cpuThreshold}%
 *
 *      [or]
 *
 *  replicas: {replica Count}
 *
 */

public class ReplicasDeserializer extends JsonDeserializer {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode replicasNode = jsonParser.readValueAsTree();
        // specNode refers to replicas
        if (replicasNode == null) {
            return defaultReplicas();
        }
        Replicas replicas = new Replicas();
        try {
            if (replicasNode.get(HyscaleSpecFields.min) != null) {
                replicas.setMin(Integer.valueOf(replicasNode.get(HyscaleSpecFields.min).toString()));
                replicas.setMax(Integer.valueOf(replicasNode.get(HyscaleSpecFields.max).toString()));
                JsonNode cpuThreshold = replicasNode.get(HyscaleSpecFields.cpuThreshold);
                if (cpuThreshold != null && !cpuThreshold.isNull()) {
                    replicas.setCpuThreshold(cpuThreshold.textValue());
                }
            } else {
                replicas.setMin(Integer.valueOf(replicasNode.toString()));
            }
        } catch (NumberFormatException e) {
            WorkflowLogger.persist(ServiceSpecActivity.FAILED_TO_DESERIALIZE_REPLICAS);
            replicas = defaultReplicas();
        }
        return replicas;
    }

    private static Replicas defaultReplicas() {
        Replicas replicas = new Replicas();
        replicas.setMin(1);
        return replicas;
    }
}
