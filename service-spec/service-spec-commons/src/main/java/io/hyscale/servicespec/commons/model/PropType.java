package io.hyscale.servicespec.commons.model;

import java.util.regex.Pattern;

/**
 * Handle different prop types based on pattern
 */
public enum PropType {

	FILE("file") {
		@Override
		public Pattern getPatternMatcher() {
			return Pattern.compile("((file)\\(.*\\))");
		}

		@Override
		public String extractPropValue(String value) {
			if (getPatternMatcher().matcher(value).matches()) {
				return value.substring(value.indexOf("(") + 1, value.length() - 1);
			}
			return value;
		}
	},
	ENDPOINT("endpoint") {
		@Override
		public Pattern getPatternMatcher() {
			return Pattern.compile("((endpoint)\\(.*\\))");
		}

		@Override
		public String extractPropValue(String value) {
			if (getPatternMatcher().matcher(value).matches()) {
				return value.substring(value.indexOf("(") + 1, value.length() - 1);
			}
			return value;
		}
	},
	STRING("string") {
		@Override
		public Pattern getPatternMatcher() {
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

	public abstract Pattern getPatternMatcher();

	public abstract String extractPropValue(String value);
}
