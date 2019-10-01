package io.hyscale.ctl.commons.utils;


import org.apache.commons.lang3.StringUtils;

public class NormalizationUtil {

    public static String normalize(String name, int length) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        String normalized = name.toLowerCase().replaceAll("[\\.]+", "-").replaceAll("[ ]+", "-")
                .replaceAll("[^a-zA-Z0-9-_]", "").replaceAll(" ", "");
        int str_length = normalized.length();
        if (str_length > length) {
            str_length = length;
        }
        return normalized.substring(0, str_length);
    }

    public static String normalize(String name) {
        if (StringUtils.isBlank(name)) {
            return name;
        }
        int length = name.length();
        return normalize(name, length);
    }

}
