package io.hyscale.ctl.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;

public class JsonSnippetConvertor {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	public static <T> String serialize(T element) throws JsonProcessingException {
		if (element == null) {
			return null;
		}
		return objectMapper.writeValueAsString(element);
	}

	public static <T> T deserialize(String element, Class<T> klazz) throws IOException {
		if (element == null) {
			return null;
		}
		return objectMapper.readValue(element, klazz);
	}

	public static <T> T deserialize(String element, TypeReference typeReference) throws IOException {
		if (element == null) {
			return null;
		}
		return objectMapper.readValue(element.getBytes(), typeReference);
	}
}
