package io.hyscale.ctl.commons.models;

public enum Status {

	DONE("DONE"), SKIPPING("SKIPPED"), FAILED("FAILED"), NOT_FOUND("NOT FOUND");

	private String message;

	private Status(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
