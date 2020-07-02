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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger logger = LoggerFactory.getLogger(ManifestTreeUtils.class);

    private ObjectMapper objectMapper;

    @Autowired
    private JsonTreeOperations jsonTreeOperations;

    @PostConstruct
    public void init() {
        this.objectMapper = ObjectMapperFactory.yamlMapper();
    }

    //TODO regex for path validation
    public JsonNode injectSnippet(String snippet, String path, ObjectNode rootNode)
            throws IOException, HyscaleException {
        if (StringUtils.isBlank(snippet)) {
            return rootNode;
        }
        if (StringUtils.isBlank(path) || rootNode == null) {
            logger.error("Path and root node required for injecting snippet");
            throw new HyscaleException(ManifestErrorCodes.ERROR_WHILE_INJECTING_MANIFEST_SNIPPET);
        }
        String parentKey = JsonTreeUtil.getParentKey(path);
        JsonNode elementNode = objectMapper.readTree(snippet);
        if (parentKey == null) {
            parentKey = "$";
            return jsonTreeOperations.put(rootNode, parentKey, JsonTreeUtil.getKey(path), elementNode);
        }
        createParentsIfNotExists(rootNode,
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
     * @param index
     * @return
     * @throws IOException
     */

    private JsonNode createParentsIfNotExists(JsonNode root, String[] paths, int index) throws IOException {
        if (root == null) {
            return root;
        }

        if (paths == null) {
            return root;
        }
        if (index >= paths.length) {
            return root;
        }

        int arrayIndex = JsonTreeUtil.getArrayIndex(paths[index]);
        JsonNode parent = root.get(normalize(paths[index]));
        if (parent == null) {
            if (JsonTreeUtil.isArrayPath(paths[index])) {
                ArrayNode arrayNode = jsonTreeOperations.arrayNode();
                if (arrayIndex >= 0) {
                    for (int j = 0; j <= arrayIndex; j++) {
                        arrayNode.add(jsonTreeOperations.objectNode());
                    }
                }
                parent = arrayNode;
            } else {
                parent = jsonTreeOperations.objectNode();
            }
            root = root.isArray() ? ((ArrayNode) root).add(parent) : ((ObjectNode) root).put(normalize(paths[index]), parent);
        } else {
            if (arrayIndex >= 0) {
                if (!parent.isArray()) {
                    throw new IOException("Parent found to be non-array node while the path claims to be an array " + paths[index]);
                }
                ArrayNode arrayNode = (ArrayNode) parent;
                for (int j = 0; j <= arrayIndex; j++) {
                    if (arrayNode.get(j) == null) {
                        arrayNode.add(jsonTreeOperations.objectNode());
                    }
                }
            }
        }
        return createParentsIfNotExists(parent, paths, index + 1);
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

    private String normalize(String path) {
        if (StringUtils.isBlank(path)) {
            return path;
        }

        if (JsonTreeUtil.isArrayPath(path)) {
            return JsonTreeUtil.getSanitizedArrayPath(path);
        }
        return path;
    }

}
