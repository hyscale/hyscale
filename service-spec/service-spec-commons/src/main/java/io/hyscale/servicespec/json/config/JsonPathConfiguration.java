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
