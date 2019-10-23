package io.hyscale.commons.logger;

public enum TableFields {

	SERVICE("Service Name", 30), STATUS("Status"), AGE("Age"), REASON("Reason"), MESSAGE("Message"),
	SERVICE_ADDRESS("Service Address", 40);

	private TableFields(String fieldName, Integer length) {
		this.fieldName = fieldName;
		this.length = length;
	}

	private TableFields(String fieldName) {
		this.fieldName = fieldName;
		this.length = TableField.DEFAULT_FIELD_LENGTH;
	}

	private String fieldName;
	private Integer length;

	public String getFieldName() {
		return fieldName;
	}

	public Integer getLength() {
		return length;
	}

}
