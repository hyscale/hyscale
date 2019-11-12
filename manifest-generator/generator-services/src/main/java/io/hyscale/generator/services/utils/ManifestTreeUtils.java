/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.generator.services.utils;

import java.io.IOException;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.node.ArrayNode;
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

@Component
public class ManifestTreeUtils {

    private ObjectMapper objectMapper;

    @Autowired
    private JsonTreeOperations jsonTreeOperations;

    @PostConstruct
    public void init() {
        this.objectMapper = ObjectMapperFactory.yamlMapper();
    }

    public JsonNode injectSnippet(String snippet, String path, ObjectNode rootNode)
            throws IOException, HyscaleException {
        if (StringUtils.isBlank(snippet)) {
            return rootNode;
        }
        String parentKey = JsonTreeUtil.getParentKey(path);
        JsonNode elementNode = objectMapper.readTree(snippet);
        if (parentKey == null) {
            parentKey = "$";
            return jsonTreeOperations.put(rootNode, parentKey, JsonTreeUtil.getKey(path), elementNode);
        }
        rootNode = (ObjectNode) createParentsIfNotExists(rootNode,
                JsonTreeUtil.getParentKey(path).split("\\."), 0);
        rootNode = (ObjectNode) createLeafNodeIfNotExists(rootNode, elementNode, path);
        return insertSnippet(rootNode, path, elementNode);
    }

    private JsonNode insertSnippet(ObjectNode rootNode, String path, JsonNode elementNode) throws HyscaleException, IOException {
        JsonNode leafNode = jsonTreeOperations.read(rootNode, path);
        if (leafNode == null) {
            throw new HyscaleException(ManifestErrorCodes.ERROR_WHILE_INJECTING_MANIFEST_SNIPPET);
        }
        if (leafNode.isArray()) {
            if (elementNode.isArray()) {
                ArrayNode arrayNode = (ArrayNode) elementNode;
                for (int i = 0; i < arrayNode.size(); i++) {
                    rootNode = (ObjectNode) jsonTreeOperations.add(rootNode, path, arrayNode.get(i));
                }
            } else {
                rootNode = (ObjectNode) jsonTreeOperations.add(rootNode, path, elementNode);
            }
        } else {
            rootNode = jsonTreeOperations.put(rootNode, JsonTreeUtil.getParentKey(path), JsonTreeUtil.getKey(path), elementNode);
        }
        return rootNode;
    }

    /**
     * Prepares the parent node of the path if array => prepare the array node of
     * the path and => if indexed , prepares the respective index element => prepare
     * the object node of the path
     *
     * @param root
     * @param paths
     * @param  index
     * @return
     * @throws IOException
     */

    private JsonNode createParentsIfNotExists(JsonNode root, String[] paths, int index) throws IOException {
        if (paths == null) {
            return root;
        }
        if (index >= paths.length) {
            return root;
        }
        String effectivePath = "$";
        for (int i = 0; i <= index; i++) {
            effectivePath += "." + paths[i];
        }
        JsonNode parent = jsonTreeOperations.read(root, effectivePath);
        if (parent == null) {
            if (JsonTreeUtil.isArrayPath(paths[index])) {
                parent = jsonTreeOperations.arrayNode();
                root = jsonTreeOperations.put(root, JsonTreeUtil.getParentKey(effectivePath),
                        JsonTreeUtil.getSanitizedArrayPath(paths[index]), parent);
                int arrayIndex = JsonTreeUtil.getArrayIndex(paths[index]);
                if (arrayIndex >= 0) {
                    for (int j = 0; j <= arrayIndex; j++) {
                        root = jsonTreeOperations.add(root,
                                JsonTreeUtil.getSanitizedArrayPath(effectivePath), jsonTreeOperations.objectNode());
                    }
                }
            } else {
                parent = jsonTreeOperations.objectNode();
                root = jsonTreeOperations.put(root, JsonTreeUtil.getParentKey(effectivePath), paths[index],
                        parent);
            }
        }
        return createParentsIfNotExists(root, paths, index + 1);
    }

    /*
     * Prepares the leaf node to inject the snippet Checks if the leaf node to be
     * inserted in an array, => if yes inserts arraynode => else inserts objectnode
     */
    private JsonNode createLeafNodeIfNotExists(JsonNode root, JsonNode elementNode, String path) throws IOException {
        if (path == null || path.isBlank()) {
            return root;
        }
        JsonNode leafNode = jsonTreeOperations.read(root, path);
        if (leafNode == null) {
            leafNode = elementNode.isArray() ? jsonTreeOperations.arrayNode() : jsonTreeOperations.objectNode();
            root = jsonTreeOperations.put(root, JsonTreeUtil.getParentKey(path), JsonTreeUtil.getKey(path), leafNode);
        }
        return root;
    }

}
