package io.hyscale.servicespec.json.config;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import io.hyscale.commons.utils.ObjectMapperFactory;

public class JsonPathConfiguration {

	private static Configuration configuration = null;

	static {
		JsonProvider jsonProvider = new JacksonJsonProvider();
		MappingProvider mappingProvider = new JacksonMappingProvider(ObjectMapperFactory.jsonMapper());
		configuration = Configuration.builder().jsonProvider(jsonProvider).mappingProvider(mappingProvider)
				.options(Option.SUPPRESS_EXCEPTIONS).build();
	}

	public static Configuration getConfiguration() {
		return configuration;
	}

}
