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
package io.hyscale.servicespec.commons.json.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.json.config.JsonPathConfiguration;
import io.hyscale.servicespec.commons.json.parser.constants.JsonPathConstants;

/**
 * Parser for json tree
 */
public class JsonTreeParser {

    private static final ObjectMapper objectMapper = ObjectMapperFactory.jsonMapper();
    private static final Logger logger = LoggerFactory.getLogger(JsonTreeParser.class);

	/**
	 * Get JsonNode for field from the root
	 * @param root
	 * @param field
	 * @return JsonNode for field
	 */

    public static JsonNode get(JsonNode root, String field) {
        if (root == null) {
            return null;
        }
        if (!field.startsWith(JsonPathConstants.JSON_ROOT_PATH)) {
            field = JsonPathConstants.JSON_ROOT_PATH + field;
        }
        return JsonPath.using(JsonPathConfiguration.getConfiguration()).parse(root.toString()).read(field,
                JsonNode.class);
    }

	/**
	 * @param root
	 * @param field
	 * @param klazz 
	 * @param <T> class object to be returned
	 * @return object of class T
	 * @throws HyscaleException
	 */

    public static <T> T get(JsonNode root, String field, Class<T> klazz) throws HyscaleException {
        if (root == null) {
            return null;
        }
        if (!field.startsWith(JsonPathConstants.JSON_ROOT_PATH)) {
            field = JsonPathConstants.JSON_ROOT_PATH + field;
        }
        return JsonPath.using(JsonPathConfiguration.getConfiguration()).parse(root.toString()).read(field, klazz);
    }

	/**
	 * @param root
	 * @param field
	 * @param typeReference
	 * @param <T> based on {@link TypeReference}
	 * @return object of class T
	 * @throws HyscaleException
	 */

    public static <T> T get(JsonNode root, String field, TypeReference<T> typeReference) throws HyscaleException {
        JsonNode jsonNode = get(root, field);
        return deserializeJsonNode(jsonNode, typeReference);
    }

	/**
	 * @param jsonNode
	 * @param klass
	 * @param <T> class object to be returned
	 * @return object of class T, null if not found
	 * @throws HyscaleException
	 */

    private static <T> T deserializeJsonNode(JsonNode jsonNode, Class<T> klass) throws HyscaleException {
        if (jsonNode == null || jsonNode.isMissingNode()) {
            return null;
        }
        try {
            return objectMapper.convertValue(jsonNode, klass);
        } catch (IllegalArgumentException e) {
            throw new HyscaleException(ServiceSpecErrorCodes.FAILED_TO_PARSE_JSON_TREE, e.getMessage());
        }
    }

	/**
	 * @param jsonNode
	 * @param typeReference
	 * @param <T> based on {@link TypeReference}
	 * @return object of class T, null if not found
	 * @throws HyscaleException
	 */

    private static <T> T deserializeJsonNode(JsonNode jsonNode, TypeReference<T> typeReference) throws HyscaleException {
        if (jsonNode == null || jsonNode.isMissingNode()) {
            return null;
        }
        try {
            return objectMapper.convertValue(jsonNode, typeReference);
        } catch (IllegalArgumentException e) {
            throw new HyscaleException(ServiceSpecErrorCodes.FAILED_TO_PARSE_JSON_TREE, e.getMessage());
        }
    }
}
