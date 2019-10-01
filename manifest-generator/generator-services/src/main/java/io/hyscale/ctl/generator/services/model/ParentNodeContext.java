package io.hyscale.ctl.generator.services.model;

import com.fasterxml.jackson.databind.JsonNode;

public class ParentNodeContext {

	private JsonNode root;
	private JsonNode parentNode;

	public ParentNodeContext(JsonNode root, JsonNode parentNode) {
		this.root = root;
		this.parentNode = parentNode;
	}

	public JsonNode getRoot() {
		return root;
	}

	public JsonNode getParentNode() {
		return parentNode;
	}

}
