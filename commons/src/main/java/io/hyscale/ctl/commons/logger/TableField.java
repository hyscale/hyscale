package io.hyscale.ctl.commons.logger;

public class TableField {

	public static final Integer DEFAULT_FIELD_LENGTH = 20;

	private String name;
	private int length = DEFAULT_FIELD_LENGTH;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLength() {
		return length;
	}

	public void setLength(Integer length) {
		if (length != null && length > 0) {
			this.length = length;
		}
	}

}
