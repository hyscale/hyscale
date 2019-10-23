package io.hyscale.commons.utils;

public class ServiceUtil {

	public static String getRandomKey(String prefix) {
		String id = IDGenerator.generate(8);
		return prefix + "-" + id;
	}

	public static String getRandomKey(String prefix, int length) {
		String id = IDGenerator.generate(length);
		return prefix + "-" + id;
	}

	public static String getRandomKeyWithNoCaps(String prefix, int length) {
		String id = IDGenerator.generateWithNoCaps(length);
		return prefix + "-" + id;
	}
}
