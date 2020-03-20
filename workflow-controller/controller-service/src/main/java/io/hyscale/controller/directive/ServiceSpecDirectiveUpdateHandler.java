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
package io.hyscale.controller.directive;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.controller.exception.ControllerErrorCodes;

/**
 * Defines update operation on service spec through json object
 * Implementation:
 * Extending class provides the object class type, update function and path
 * from the parent
 * 
 * @author tushart
 *
 */
public abstract class ServiceSpecDirectiveUpdateHandler<T> {

	private static final Logger logger = LoggerFactory.getLogger(ServiceSpecDirectiveUpdateHandler.class);

	protected static ObjectMapper mapper = ObjectMapperFactory.jsonMapper();

	/**
	 * Performs inplace update on passed object node
	 * 
	 * @param serviceSpecObjNode - this will be updated
	 */
	public void update(ObjectNode serviceSpecObjNode) throws HyscaleException {

		T t = getModel(serviceSpecObjNode);
		if (t == null) {
			return;
		}
		int initialHash = t.hashCode();
		t = updateObject(t);
		int updatedHash = t.hashCode();

		if (initialHash != updatedHash) {
			updateDirective(serviceSpecObjNode, t);
		}
	}

	/**
	 * Replaces updated object in serviceSpecObject at path provided by
	 * {@link #getPath()}
	 * 
	 * @param serviceSpecObject
	 * @param updatedObject
	 */
	protected void updateDirective(ObjectNode serviceSpecObject, Object updatedObject) {
		if (serviceSpecObject == null) {
			return;
		}
		String[] path = getPath();
		// get parent node
		JsonNode jsonNode = getNodeAtPath(serviceSpecObject, Arrays.copyOfRange(path, 0, path.length - 1));
		if (jsonNode == null) {
			return;
		}
		String field = path[path.length - 1];
		if (jsonNode.get(field) == null) {
			return;
		}
		ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		ObjectNode parentObjNode = (ObjectNode) jsonNode;
		parentObjNode.replace(field, mapper.valueToTree(updatedObject));
	}

	/**
	 * Fetch json object based on {@link #getPath()} from service spec node
	 * Converts json object to java object
	 * 
	 * @param serviceSpecObjNode
	 * @return POJO, null if not found
	 * @throws HyscaleException
	 */
	protected T getModel(ObjectNode serviceSpecObjNode) throws HyscaleException {
		if (serviceSpecObjNode == null) {
			return null;
		}
		if (getPath() == null) {
			return null;
		}
		JsonNode jsonNode = getNodeAtPath(serviceSpecObjNode, getPath());
		if (jsonNode == null || jsonNode.isNull()) {
			return null;
		}
		T t = null;
		try {
			t = mapper.treeToValue(jsonNode, getType());
		} catch (JsonProcessingException e) {
			logger.error("Error while processing service spec ", e);
			throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_PROCESSING_FAILED, e.getMessage());
		}

		return t;
	}
	
	/**
	 * 
	 * @param parentObjectNode
	 * @param path
	 * @return JsonNode present at path, null if absent
	 */
	private JsonNode getNodeAtPath(ObjectNode parentObjectNode, String... path) {
		JsonNode jsonNode = parentObjectNode;
		for (int i = 0; i < path.length; i++) {
			if (jsonNode == null || jsonNode.isNull()) {
				return null;
			}
			jsonNode = jsonNode.get(path[i]);
		}
		return jsonNode;
	}

	/**
	 * 
	 * @return class of implementing type
	 */
	protected abstract Class<T> getType();

	/**
	 * Implementing class defines what all fields to update in the service spec
	 * 
	 * @param t
	 * @return updated object
	 * @throws HyscaleException
	 */
	protected abstract T updateObject(T t) throws HyscaleException;

	/**
	 * Implementing class defines the path
	 * 
	 * @return path to field from parent
	 */
	protected abstract String[] getPath();

}
