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
package io.hyscale.schema.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by vijays on 18/9/19.
 * Compares input String,JsonNode with the reference Json Schema passed and returns report.
 */
public class JsonSchemaValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaValidator.class);
    private static final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

    /**
     * Validates whether the given input string satisfies schema.
     *
     * @param inputServiceSpec String input
     * @param schema Reference Json Schema
     * @return ProcessingReport
     * @throws HyscaleException
     */
    public static ProcessingReport validate(String inputServiceSpec, String schema) throws HyscaleException {
        try {
            JsonNode inputServiceSpecJsonNode = JsonLoader.fromString(inputServiceSpec);
            return validate(inputServiceSpecJsonNode,schema);
        }catch (IOException e) {
            LOGGER.error(e.getMessage());
            HyscaleException ex = new HyscaleException(e,CommonErrorCode.STRING_TO_JSON_NODE_CONVERSION_FAILURE,e.getMessage());
            throw ex;
        }
    }

    /**
     * Validates whether the given input json node satisfies schema.
     *
     * @param inputServiceSpec JsonNode input
     * @param schemaPath Reference Json Schema
     * @return ProcessingReport
     * @throws HyscaleException
     */
    public static ProcessingReport validate(JsonNode inputServiceSpec, String schemaPath) throws HyscaleException{
        try {
            JsonNode schemaJsonNode = JsonLoader.fromResource(schemaPath);
            return validate(inputServiceSpec,schemaJsonNode);
        }catch (IOException i){
            LOGGER.error(i.getMessage());
            throw new HyscaleException(i,CommonErrorCode.FAILED_TO_READ_FILE,schemaPath);
        }
    }

    public static ProcessingReport validate(JsonNode inputServiceSpec,JsonNode referenceSchema) throws HyscaleException{
        try {
            JsonSchema schema = factory.getJsonSchema(referenceSchema);
            return schema.validate(inputServiceSpec);
        }catch (ProcessingException p){
            LOGGER.error(p.getMessage());
            throw new HyscaleException(p,CommonErrorCode.SCHEMA_PROCESSING_ERROR);
        }catch (NullPointerException n){
            LOGGER.error(n.getMessage());
            throw new HyscaleException(n,CommonErrorCode.NULL_SCHEMA);
        }
    }
}
