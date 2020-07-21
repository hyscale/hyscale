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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleError;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;

/**
 * To test JsonNode injectSnippet(String snippet, String path, ObjectNode rootNode)
 * Cases to cover:
 * Null values
 * Injecting in array
 * Normal injecting
 * Injecting in empty json
 * 
 */
@SpringBootTest
class ManifestTreeUtilsTest {

    private static final ObjectNode OBJECT_NODE = JsonNodeFactory.instance.objectNode();

    private ObjectMapper objectMapper = ObjectMapperFactory.jsonMapper();

    @Autowired
    private ManifestTreeUtils manifestTreeUtils;

    private static Stream<Arguments> exceptionInput() {
        return Stream.of(
                Arguments.of("test", null, OBJECT_NODE, ManifestErrorCodes.ERROR_WHILE_INJECTING_MANIFEST_SNIPPET),
                Arguments.of("test", "root", null, ManifestErrorCodes.ERROR_WHILE_INJECTING_MANIFEST_SNIPPET));
    }

    @ParameterizedTest
    @MethodSource("exceptionInput")
    void testException(String snippet, String path, ObjectNode objectNode, HyscaleError error) {
        try {
            manifestTreeUtils.injectSnippet(snippet, path, objectNode);
        } catch (HyscaleException e) {
            if (e.getHyscaleError() == error) {
                return;
            }
            fail(e);
        } catch (IOException e) {
            fail(e);
        }
        fail("No exception thrown, expected exception: " + error.getMessage());
    }

    private static Stream<Arguments> input() throws JsonProcessingException {
        Map<String, String> snippetMap = new HashMap<>();
        snippetMap.put("test", "value");
        return Stream.of(
                Arguments.of(JsonSnippetConvertor.serialize(snippetMap),
                        "path", null, getInputDir("empty-injected.json")), 
                Arguments.of(JsonSnippetConvertor.serialize(snippetMap),
                        "path", getInputDir("normal.json"), getInputDir("normal-injected.json")), 
                Arguments.of(JsonSnippetConvertor.serialize(snippetMap),
                        "test.testleaf", getInputDir("leaf-source.json"), getInputDir("leaf-injected-replace.json")),
                Arguments.of(JsonSnippetConvertor.serialize(snippetMap),
                        "test.newtestleaf", getInputDir("leaf-source.json"), getInputDir("leaf-injected-new.json")),
                Arguments.of(JsonSnippetConvertor.serialize(snippetMap),
                        "root.test[1].new", getInputDir("array-source.json"), getInputDir("array-injected-new.json")),
                Arguments.of(JsonSnippetConvertor.serialize(snippetMap),
                        "root.test[0].new", getInputDir("array-source.json"), getInputDir("array-injected-replace.json")),
                Arguments.of(JsonSnippetConvertor.serialize(snippetMap),
                        "root.test[2].new", getInputDir("array-source.json"), getInputDir("array-injected-skip.json"))
                );
    }

    @ParameterizedTest
    @MethodSource("input")
    void testSnippetInjection(String snippet, String path, String sourceNodeFile, String expectedJsonFile)
            throws HyscaleException, IOException {
        ObjectNode objectNode = OBJECT_NODE;
        if (StringUtils.isNotBlank(sourceNodeFile)) {
            objectNode = (ObjectNode) objectMapper.readTree(new File(getFilePath(sourceNodeFile)));
        }
        try {
            JsonNode data = manifestTreeUtils.injectSnippet(snippet, path, objectNode);
            String expectedData = getData(expectedJsonFile);
            JSONAssert.assertEquals("Expected data doesn't match with actual data.", expectedData, data.toString(),
                    false);
        } catch (IOException | HyscaleException | JSONException e) {
            fail(e);
        }
    }

    private String getData(String path) throws HyscaleException {
        return HyscaleFilesUtil.readFileData(new File(getFilePath(path)));
    }

    private String getFilePath(String path) {
        URL urlPath = ManifestTreeUtilsTest.class.getResource(path);
        return urlPath.getFile();
    }

    private static String getInputDir(String fileName) {
        return "/util-input/manifest-injection/" + fileName;
    }
}
