package io.hyscale.ctl.servicespec.commons.model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import io.hyscale.ctl.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.ctl.servicespec.json.parser.JsonTreeParser;

import java.io.IOException;

public final class ServiceSpec {

    private JsonNode root;

    public ServiceSpec(JsonNode root) {
        this.root = root;
    }

    public ServiceSpec(String serviceSpec) throws HyscaleException {
        ObjectMapper mapper = ObjectMapperFactory.yamlMapper();
        try {
            this.root = mapper.readTree(serviceSpec);
        } catch (IOException e) {
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_PARSE_ERROR);
        }
    }

    /**
     * @param path
     * @return
     */
    public JsonNode get(String path) {
        return JsonTreeParser.get(root, path);
    }

    /**
     * @param path
     * @param klass
     * @param <T>
     * @return
     */

    public <T> T get(String path, Class<T> klass) throws HyscaleException {
        return JsonTreeParser.get(root, path, klass);
    }

    /**
     * @param path
     * @param typeReference
     * @param <T>
     * @return
     */

    public <T> T get(String path, TypeReference typeReference) throws HyscaleException {
        return JsonTreeParser.get(root, path, typeReference);
    }

    @Override
    public String toString() {
        ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
        try {
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
