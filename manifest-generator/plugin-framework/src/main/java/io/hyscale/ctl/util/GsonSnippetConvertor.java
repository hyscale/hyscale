package io.hyscale.ctl.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.reflect.Type;

public class GsonSnippetConvertor {

	private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

	public static <T> String serialize(T element) throws JsonProcessingException {
		if (element == null) {
			return null;
		}
		return gson.toJson(element);
	}

	public static <T> T deserialize(String element, Class<T> klazz) throws IOException {
		if (element == null) {
			return null;
		}
		return gson.fromJson(element, klazz);
	}

	public static <T> T deserialize(String element, Type type) throws IOException {
		if (element == null) {
			return null;
		}
		return gson.fromJson(element, type);
	}
}
