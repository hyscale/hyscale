package io.hyscale.ctl.generator.services.model;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class ManifestNode {

	private ObjectNode objectNode;

	public void setObjectNode(ObjectNode objectNode) {
		this.objectNode = objectNode;
	}

	public ManifestNode(ObjectNode objectNode) {
		this.objectNode = objectNode;
	}

	public ObjectNode getObjectNode() {
		return objectNode;
	}
}
