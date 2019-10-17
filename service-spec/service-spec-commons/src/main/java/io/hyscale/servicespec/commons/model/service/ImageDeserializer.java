package io.hyscale.servicespec.commons.model.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.json.parser.JsonTreeParser;
import io.hyscale.servicespec.commons.model.service.BuildSpecImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ImageDeserializer extends JsonDeserializer {

    private static final Logger logger = LoggerFactory.getLogger(ImageDeserializer.class);

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode specNode = jsonParser.readValueAsTree();
        BuildSpecImage buildSpecImage = new BuildSpecImage();
        if(specNode.has("buildSpec")){
            ObjectMapper objectMapper = new ObjectMapper();
            buildSpecImage = objectMapper.readValue(specNode.toString(), BuildSpecImage.class);
        }
        return buildSpecImage;
    }
}
