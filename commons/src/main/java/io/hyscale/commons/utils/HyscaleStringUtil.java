package io.hyscale.commons.utils;

import org.apache.commons.lang3.StringUtils;

public class HyscaleStringUtil {
	
	public static String removeSuffixStr(String input, String trailingStr) {
		if (StringUtils.isBlank(input) || StringUtils.isBlank(trailingStr)) {
			return input;
		}
		if (input.endsWith(trailingStr)) {
			return input.substring(0, input.lastIndexOf(trailingStr));
		}
        return input;
	}
	
	public static String removeSuffixStr(StringBuilder input, String trailingStr) {
		if (input == null) {
			return null;
		}
		return removeSuffixStr(input.toString(), trailingStr);
	}
	
	public static String removeSuffixChar(StringBuilder input, char trailingChar) {
		if (input == null) {
			return null;
		}
		return removeSuffixStr(input.toString(), Character.toString(trailingChar));
	}

}
