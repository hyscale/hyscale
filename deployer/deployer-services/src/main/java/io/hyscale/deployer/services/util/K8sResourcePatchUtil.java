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
package io.hyscale.deployer.services.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.GsonProviderUtil;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;

/**
 * Utility for resource path operations
 *
 */
public class K8sResourcePatchUtil {

    private K8sResourcePatchUtil() {}
    
	/**
	 * Creates Json diff for patch based on source and target
	 * @param <T> 
	 * @param source
	 * @param target
	 * @param klass
	 * @return patched object, null if source and target are not instances of same class
	 * @throws HyscaleException
	 */
	public static <T> Object getJsonPatch(T source, T target, Class<T> klass) throws HyscaleException {
		if ((klass.isInstance(source) && klass.isInstance(target))) {
			String patchString = getzJsonPatch(source, target);
			return deserialize(patchString, JsonElement.class);
		}
		return null;
	}

	private static String getzJsonPatch(Object source, Object target) throws HyscaleException {
		ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
		Gson gson = new Gson();
		JsonNode patch;
		try {
			patch = JsonDiff.asJson(mapper.readTree(gson.toJson(source)), mapper.readTree(gson.toJson(target)));
			return mapper.writeValueAsString(patch);
		} catch (IOException e) {
			throw new HyscaleException(e, DeployerErrorCodes.ERROR_WHILE_CREATING_PATCH);
		}
	}

	public static Object deserialize(String jsonStr, Class<?> targetClass) {
	    return GsonProviderUtil.getPrettyGsonBuilder().fromJson(jsonStr, targetClass);
	}
}
