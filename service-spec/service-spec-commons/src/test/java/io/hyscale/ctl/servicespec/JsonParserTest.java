package io.hyscale.ctl.servicespec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import io.hyscale.ctl.servicespec.json.parser.JsonTreeParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


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
