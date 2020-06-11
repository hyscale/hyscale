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

import java.io.File;
import java.io.IOException;

/**
 * Created by vijays on 18/9/19.
 * Compares input spec with the reference Json Schema passed and returns report.
 */
public class JsonSchemaValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonSchemaValidator.class);
    private static final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

    /**
     * Validates whether the given input string satisfies schema.
     *
     * @param inputSpec String input
     * @param schema Reference Json Schema String Input
     * @return ProcessingReport
     * @throws HyscaleException
     */
    public static ProcessingReport validate(String inputSpec, String schema) throws HyscaleException {
        try {
            JsonNode inputSpecJsonNode = JsonLoader.fromString(inputSpec);
            JsonNode schemaNode = JsonLoader.fromString(schema);
            return validate(inputSpecJsonNode,schemaNode);
        }catch (IOException e) {
            LOGGER.error(e.getMessage());
            HyscaleException ex = new HyscaleException(e,CommonErrorCode.ERROR_OCCURED_WHILE_SCHEMA_VALIDATION,e.getMessage());
            throw ex;
        }
    }

    /**
     * validates whether the given input spec file satisfies schema
     *
     * @param inputSpecFile
     * @param referenceSchemaFile
     * @return ProcessingReport
     * @throws HyscaleException
     */
    public static ProcessingReport validate(File inputSpecFile, File referenceSchemaFile) throws HyscaleException{
        try {
            JsonNode inputSpecJsonNode = JsonLoader.fromFile(inputSpecFile);
            JsonNode schemaNode = JsonLoader.fromFile(referenceSchemaFile);
            return validate(inputSpecJsonNode,schemaNode);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            HyscaleException ex = new HyscaleException(e,CommonErrorCode.ERROR_OCCURED_WHILE_SCHEMA_VALIDATION,e.getMessage());
            throw ex;
        }
    }

    /**
     * Validates whether the given input json node satisfies schema.
     *
     * @param inputSpecNode JsonNode input
     * @param referenceSchema JsonNode input
     * @return ProcessingReport
     * @throws HyscaleException
     */
    public static ProcessingReport validate(JsonNode inputSpecNode,JsonNode referenceSchema) throws HyscaleException{
        if(referenceSchema==null || referenceSchema.isNull()){
            LOGGER.error(CommonErrorCode.EMPTY_REFERENCE_SCHEMA_FOUND.getMessage());
            throw new HyscaleException(CommonErrorCode.EMPTY_REFERENCE_SCHEMA_FOUND);
        }
        try {
            JsonSchema schema = factory.getJsonSchema(referenceSchema);
            return schema.validate(inputSpecNode, true);
        }catch (ProcessingException p){
            LOGGER.error(p.getMessage());
            throw new HyscaleException(p,CommonErrorCode.SCHEMA_PROCESSING_ERROR);
        }
    }

}
