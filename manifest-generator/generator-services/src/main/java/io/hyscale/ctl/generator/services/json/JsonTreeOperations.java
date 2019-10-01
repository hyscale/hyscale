package io.hyscale.ctl.generator.services.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.hyscale.ctl.servicespec.json.config.JsonPathConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
public class JsonTreeOperations {

	private ObjectMapper objectMapper;

	@PostConstruct
	public void init() {
		this.objectMapper = new ObjectMapper(new YAMLFactory());
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
