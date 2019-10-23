package io.hyscale.commons.utils;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ListToMapDeserializer extends JsonDeserializer<HashMap<String, String>> {

	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";

	@Override
	public HashMap<String, String> deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {

		HashMap<String, String> ret = new HashMap<String, String>();

		ObjectCodec codec = parser.getCodec();
		TreeNode node = codec.readTree(parser);

		if (node.isArray()) {
			for (JsonNode n : (ArrayNode) node) {
				JsonNode id = n.get(KEY_FIELD);
				if (id != null) {
					JsonNode name = n.get(VALUE_FIELD);
					ret.put(id.asText(), name.asText());
				}
			}
		}
		return ret;
	}

}
