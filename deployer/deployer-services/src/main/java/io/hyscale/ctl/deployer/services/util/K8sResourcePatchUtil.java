package io.hyscale.ctl.deployer.services.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import io.hyscale.ctl.deployer.services.exception.DeployerErrorCodes;

public class K8sResourcePatchUtil {

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
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.ERROR_WHILE_CREATING_PATCH);
			throw ex;
		}
	}

	private static Object deserialize(String jsonStr, Class<?> targetClass) {
		Gson gson = new Gson();
		Object obj = gson.fromJson(jsonStr, targetClass);
		return obj;
	}
}
