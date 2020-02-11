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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.activity.ServiceSpecActivity;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.json.parser.JsonTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Defines {@link ServiceSpec} as tree of JsonNode
 *
 * @see <a href="https://github.com/hyscale/hspec/blob/master/docs/hyscale-spec-reference.md">Spec Reference</a>
 *
 */
public final class ServiceSpec implements HyscaleSpec {

    private static final Logger LOGGER = LoggerFactory.getLogger(HyscaleSpec.class);

    private JsonNode root;

    public ServiceSpec(JsonNode root) {
        this.root = root;
    }

    public ServiceSpec(String serviceSpec) throws HyscaleException {
        ObjectMapper mapper = ObjectMapperFactory.yamlMapper();
        try {
            this.root = mapper.readTree(serviceSpec);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new HyscaleException(e,ServiceSpecErrorCodes.SERVICE_SPEC_PARSE_ERROR);
        }
    }

    public JsonNode get(String path) {
        return JsonTreeParser.get(root, path);
    }

    public <T> T get(String path, Class<T> klass) throws HyscaleException {
        return JsonTreeParser.get(root, path, klass);
    }

    public <T> T get(String path, TypeReference<T> typeReference) throws HyscaleException {
        return JsonTreeParser.get(root, path, typeReference);
    }

    @Override
    public String toString() {
        ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
