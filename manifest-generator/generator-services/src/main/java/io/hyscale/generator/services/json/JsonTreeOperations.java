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
package io.hyscale.generator.services.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.json.config.JsonPathConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class JsonTreeOperations {

	private ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		this.objectMapper = ObjectMapperFactory.yamlMapper();
	}

	public JsonNode read(JsonNode node, String path) {
		return JsonPath.using(JsonPathConfiguration.getConfiguration()).parse(node.toString()).read(path,
				JsonNode.class);
	}

	public JsonNode add(JsonNode node, String path, JsonNode elementNode) throws IOException {
		DocumentContext documentContext = JsonPath.using(JsonPathConfiguration.getConfiguration())
				.parse(node.toString()).add(path, elementNode);
		return objectMapper.readTree(documentContext.jsonString());
	}

	public ObjectNode put(JsonNode node, String parentPath, String key, JsonNode leafNode) throws IOException {
		DocumentContext doccumentContext = JsonPath.using(JsonPathConfiguration.getConfiguration())
				.parse(node.toString()).put(parentPath, key, leafNode);
		return (ObjectNode) objectMapper.readTree(doccumentContext.jsonString());
	}

	public ArrayNode arrayNode() {
		return JsonNodeFactory.instance.arrayNode();
	}

	public ObjectNode objectNode() {
		return JsonNodeFactory.instance.objectNode();
	}

}
