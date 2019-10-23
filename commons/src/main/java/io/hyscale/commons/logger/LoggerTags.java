package io.hyscale.commons.logger;

public enum LoggerTags {

	USER_INFO_TAG("[INFO]"), ERROR("[ERROR]"), WARN("[WARN]"), VERBOSE("[VERBOSE]"), DEBUG("[DEBUG]"),
	ACTION("[ACTION]"),;

	private String tag;

	LoggerTags(String s) {
		this.tag = s;
	}

	public String getTag() {
		return tag;
	}
}
