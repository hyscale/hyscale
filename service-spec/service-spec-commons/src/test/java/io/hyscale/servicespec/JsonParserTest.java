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
package io.hyscale.servicespec;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.json.parser.JsonTreeParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;


public class JsonParserTest {

    static JsonNode rootNode;

    @BeforeAll
    public static void init() throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
        String sampleJson = "/test-data/test.json";
        InputStream resourceAsStream = BaseFieldsTest.class.getResourceAsStream(sampleJson);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        rootNode = mapper.readTree(testData);
    }

    @Test
    public void testGetField() {
        JsonNode vegetable = JsonTreeParser.get(rootNode, "food.vegetables[0]");
        Assertions.assertNotNull(vegetable);
        Assertions.assertEquals("Brinjal", vegetable.asText());
    }

    @Test
    public void testGetFieldWithType() throws HyscaleException {
        String vegetable = JsonTreeParser.get(rootNode, "food.vegetables[1]", String.class);
        Assertions.assertEquals("Tomato", vegetable);
        String color = JsonTreeParser.get(rootNode, "food.fruits[1].color", String.class);
        Assertions.assertEquals("yellow", color);
    }
}
