package io.hyscale.deployer.core.model;

public enum ResourceOperation {

	GET("Get"), CREATE("Create"), UPDATE("Update"), DELETE("Delete"), PATCH("Patch"),
	GET_BY_SELECTOR("Get by Selector"), DELETE_BY_SELECTOR("Delete by selector");

	private String operation;

	ResourceOperation(String operation) {
		this.operation = operation;
	}

	public String getOperation() {
		return this.operation;
	}
}
