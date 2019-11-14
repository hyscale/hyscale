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
package io.hyscale.controller.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.directive.ServiceSpecDirectiveUpdateHandler;

/**
 * Util class to provide operations on service spec
 * like support windows based file separator
 * 
 * @author tushart
 *
 */
@Component
public class EffectiveServiceSpecUtil {

	private static final Logger logger = LoggerFactory.getLogger(EffectiveServiceSpecUtil.class);
	
	@Autowired
	private List<ServiceSpecDirectiveUpdateHandler> serviceSpecUpdateHandlers;

	/**
	 * Takes service spec as Object node updates file separator to unix based file
	 * system
	 * 
	 * @param serviceSpecObjNode
	 * @return non modifiable JsonNode with updated file separators
	 * @throws HyscaleException
	 */
	public JsonNode updateFilePath(ObjectNode serviceSpecObjNode) throws HyscaleException {
		logger.debug("Service spec directive update handlers available: {}", serviceSpecUpdateHandlers);
		for (ServiceSpecDirectiveUpdateHandler serviceSpecUpdateHandler : serviceSpecUpdateHandlers) {
			serviceSpecUpdateHandler.update(serviceSpecObjNode);
		}

		return serviceSpecObjNode;

	}

}
