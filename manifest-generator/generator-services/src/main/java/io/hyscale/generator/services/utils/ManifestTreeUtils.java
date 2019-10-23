package io.hyscale.generator.services.utils;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.json.JsonTreeOperations;
import io.hyscale.generator.services.json.JsonTreeUtil;
import io.hyscale.generator.services.model.ParentNodeContext;

@Component
public class ManifestTreeUtils {

	private ObjectMapper objectMapper;

	@Autowired
	private JsonTreeOperations jsonTreeOperations;

	@PostConstruct
	public void init() {
		this.objectMapper = ObjectMapperFactory.yamlMapper();
	}

	public JsonNode injectSnippet(String snippet, String path, ObjectNode manifestRootNode)
			throws IOException, HyscaleException {
		if (StringUtils.isBlank(snippet)) {
			return manifestRootNode;
		}
		String parentKey = JsonTreeUtil.getParentKey(path);
		JsonNode snippetNode = objectMapper.readTree(snippet);
		if (parentKey == null) {
			parentKey = "$";
			return jsonTreeOperations.put(manifestRootNode, parentKey, JsonTreeUtil.getKey(path), snippetNode);
		}
		ParentNodeContext parentContext = createParentsIfNotExists(manifestRootNode, JsonTreeUtil.getParentKey(path));
		if (parentContext != null && parentContext.getRoot() != null) {
			manifestRootNode = (ObjectNode) parentContext.getRoot();
			// TODO check if the parent node is an arraynode or not , if yes add the element
			// to the node if not inject
			manifestRootNode = (ObjectNode) createLeafNodeIfNotExists(manifestRootNode, snippetNode, path);
			if (parentContext.getParentNode() != null) {
				return jsonTreeOperations.put(manifestRootNode, parentKey, JsonTreeUtil.getKey(path), snippetNode);
			} else {
				throw new HyscaleException(ManifestErrorCodes.ERROR_WHILE_INJECTING_MANIFEST_SNIPPET);
			}
		} else {
			throw new HyscaleException(ManifestErrorCodes.ERROR_WHILE_INJECTING_MANIFEST_SNIPPET);
		}
	}

	/**
	 * Prepares the parent node of the path if array => prepare the array node of
	 * the path and => if indexed , prepares the respective index element => prepare
	 * the object node of the path
	 *
	 * @param jsonNode
	 * @param field
	 * @return
	 * @throws IOException
	 */

	private ParentNodeContext createParentsIfNotExists(JsonNode jsonNode, String field) throws IOException {
		if (field == null) {
			return null;
		}
		if (!field.startsWith("$")) {
			field = "$." + field;
		}
		String[] paths = field.substring(2).split("\\.");
		// As $ will be the first path
		String effectivePath = "$";
		JsonNode parent = jsonNode;
		for (int i = 0; i < paths.length; i++) {
			effectivePath += "." + paths[i];

			parent = jsonTreeOperations.read(jsonNode, effectivePath);
			if (parent == null) {
				if (JsonTreeUtil.isArrayPath(paths[i])) {
					parent = jsonTreeOperations.arrayNode();
					jsonNode = jsonTreeOperations.put(jsonNode, JsonTreeUtil.getParentKey(effectivePath),
							JsonTreeUtil.getSanitizedArrayPath(paths[i]), parent);
					int index = JsonTreeUtil.getArrayIndex(paths[i]);
					if (index >= 0) {
						for (int j = 0; j <= index; j++) {
							jsonNode = jsonTreeOperations.add(jsonNode,
									JsonTreeUtil.getSanitizedArrayPath(effectivePath), jsonTreeOperations.objectNode());
						}
					}
				} else {

					parent = jsonTreeOperations.objectNode();
					jsonNode = jsonTreeOperations.put(jsonNode, JsonTreeUtil.getParentKey(effectivePath), paths[i],
							parent);
				}
			}
		}
		return new ParentNodeContext(jsonNode, parent);
	}

	/*
	 * Prepares the leaf node to inject the snippet Checks if the leaf node to be
	 * inserted is an array, => if yes inserts arraynode => else inserts objectnode
	 */
	private JsonNode createLeafNodeIfNotExists(JsonNode root, JsonNode leafNode, String path) throws IOException {
		if (jsonTreeOperations.read(root, path) == null) {
			JsonNode leafParent = null;
			if (leafNode.isArray()) {
				leafParent = jsonTreeOperations.arrayNode();
			} else {
				leafParent = jsonTreeOperations.objectNode();
			}
			root = jsonTreeOperations.put(root, JsonTreeUtil.getParentKey(path), JsonTreeUtil.getKey(path), leafParent);
		}
		return root;
	}

}
