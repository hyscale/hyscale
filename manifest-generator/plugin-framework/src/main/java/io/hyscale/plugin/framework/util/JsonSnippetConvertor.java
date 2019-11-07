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
package io.hyscale.plugin.framework.util;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	public static <T> T deserialize(String element, TypeReference<T> typeReference) throws IOException {
		if (element == null) {
			return null;
		}
		return objectMapper.readValue(element.getBytes(), typeReference);
	}
}
