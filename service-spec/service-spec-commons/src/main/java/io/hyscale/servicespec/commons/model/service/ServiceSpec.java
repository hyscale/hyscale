package io.hyscale.servicespec.commons.model.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.json.parser.JsonTreeParser;

import java.io.IOException;

/**
 * Defines {@link ServiceSpec} as tree of JsonNode
 *
 */
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
     * Get JsonNode for field defined by path from the root
     * @param path
     * @return JsonNode of field at path
     */
    public JsonNode get(String path) {
        return JsonTreeParser.get(root, path);
    }

    /**
     * Get Object for field defined by path from the root
     * @param <T> class object to be returned
     * @param path
     * @param klass
     * @return object of class T
     * @throws HyscaleException
     */
    public <T> T get(String path, Class<T> klass) throws HyscaleException {
        return JsonTreeParser.get(root, path, klass);
    }

    /**
     * Get Object for field defined by path from the root
     * @param <T>
     * @param path
     * @param typeReference - defines class object (T) to be returned
     * @return object of class T
     * @throws HyscaleException
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
