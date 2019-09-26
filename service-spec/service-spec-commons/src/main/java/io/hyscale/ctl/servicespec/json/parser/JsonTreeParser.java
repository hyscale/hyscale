package io.hyscale.ctl.servicespec.json.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import io.hyscale.ctl.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.ctl.servicespec.json.config.JsonPathConfiguration;
import io.hyscale.ctl.servicespec.json.parser.constants.JsonPathConstants;

/**
 * Parser for json tree
 */
public class JsonTreeParser {

	private static final ObjectMapper objectMapper = ObjectMapperFactory.jsonMapper();
	private static final Logger logger = LoggerFactory.getLogger(JsonTreeParser.class);

	/**
	 * @param root
	 * @param field
	 * @return
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
	 * @param <T>
	 * @return
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
	 * @param <T>
	 * @return
	 * @throws HyscaleException
	 */

	public static <T> T get(JsonNode root, String field, TypeReference typeReference) throws HyscaleException {
		JsonNode jsonNode = get(root, field);
		return deserializeJsonNode(jsonNode, typeReference);
	}

	/**
	 * @param jsonNode
	 * @param klass
	 * @param <T>
	 * @return
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
	 * @param <T>
	 * @return
	 * @throws HyscaleException
	 */

	private static <T> T deserializeJsonNode(JsonNode jsonNode, TypeReference typeReference) throws HyscaleException {
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
