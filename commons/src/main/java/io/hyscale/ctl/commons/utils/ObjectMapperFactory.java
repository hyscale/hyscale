package io.hyscale.ctl.commons.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ObjectMapperFactory {

	private static ObjectMapper jsonObjectMapper;

	private static ObjectMapper yamlObjectMapper;

	public static ObjectMapper jsonMapper() {
		jsonObjectMapper = new ObjectMapper();

		return defaultConfig(jsonObjectMapper);
	}

	public static ObjectMapper yamlMapper() {
		yamlObjectMapper = new ObjectMapper(new YAMLFactory());

		return defaultConfig(yamlObjectMapper);
	}

	private static ObjectMapper defaultConfig(ObjectMapper objectMapper) {
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		return objectMapper;
	}
}
