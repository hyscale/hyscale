package io.hyscale.ctl.servicespec.commons.model;

import java.util.regex.Pattern;

public enum PropType {

	FILE("file") {
		@Override
		public Pattern getPatterMatcher() {
			return Pattern.compile("((file)\\(.*\\))");
		}

		@Override
		public String extractPropValue(String value) {
			if (getPatterMatcher().matcher(value).matches()) {
				return value.substring(value.indexOf("(") + 1, value.length() - 1);
			}
			return value;
		}
	},
	ENDPOINT("endpoint") {
		@Override
		public Pattern getPatterMatcher() {
			return Pattern.compile("((endpoint)\\(.*\\))");
		}

		@Override
		public String extractPropValue(String value) {
			if (getPatterMatcher().matcher(value).matches()) {
				return value.substring(value.indexOf("(") + 1, value.length() - 1);
			}
			return value;
		}
	},
	STRING("string") {
		@Override
		public Pattern getPatterMatcher() {
			return Pattern.compile("^(?!((file|endpoint)\\(.*\\))).*");
		}

		@Override
		public String extractPropValue(String value) {
			Pattern pattern = Pattern.compile("((string)\\(.*\\))");
			if (pattern.matcher(value).matches()) {
				return value.substring(value.indexOf("(") + 1, value.length() - 1);
			}
			return value;
		}
	};

	private String propType;

	PropType(String propType) {
		this.propType = propType;
	}

	public String getPropType() {
		return propType;
	}

	public abstract Pattern getPatterMatcher();

	public abstract String extractPropValue(String value);
}
