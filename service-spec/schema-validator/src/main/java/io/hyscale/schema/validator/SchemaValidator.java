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

import com.github.fge.jsonschema.core.report.ProcessingReport;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.HyscaleSpecType;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Processes given input spec file,gets respective reference schema and returns validated ProcessingReport from JsonSchemaValidator.
 */
@Component
public class SchemaValidator {
    @Autowired
    private BuildProperties buildProperties;

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidator.class);

    /**
     * validates whether the given string satisfies respective reference schema.
     *
     * @param spec
     * @param type
     * @return ProcessingReport
     * @throws HyscaleException
     */
    public ProcessingReport validateSpec(String spec, HyscaleSpecType type) throws HyscaleException {
        return JsonSchemaValidator.validate(spec,getSchema(type));
    }

    /**
     * validates whether the given spec file satisfies respective reference schema.
     *
     * @param specFile
     * @param type
     * @return ProcessingReport
     * @throws HyscaleException
     */
    public ProcessingReport validateSpec(File specFile,HyscaleSpecType type) throws HyscaleException {
        try {
            return validateSpec(FileUtils.readFileToString(specFile,ToolConstants.CHARACTER_ENCODING),type);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            HyscaleException ex = new HyscaleException(e,CommonErrorCode.FAILED_TO_READ_FILE,specFile.getPath());
            throw ex;
        }
    }

    private String getSchemaPath(HyscaleSpecType type){
        StringBuilder schemaPathBuilder = new StringBuilder();
        if(type == HyscaleSpecType.SERVICE){
         schemaPathBuilder.append("/hspec/").append(buildProperties.get(ToolConstants.HSPEC_VERSION)).append("/service-spec.json");
        }else if (type == HyscaleSpecType.ENVIRONMENT){
            schemaPathBuilder.append("/hprof/").append(buildProperties.get(ToolConstants.HSPEC_VERSION)).append("/profile-spec.json");
        }
        return schemaPathBuilder.toString();
    }

    private String getSchema(HyscaleSpecType type) throws HyscaleException{
        String schemaPath = getSchemaPath(type);
        InputStream is = SchemaValidator.class.getClassLoader().getResourceAsStream(schemaPath);
        if(is==null){
            throw new HyscaleException(CommonErrorCode.FAILED_TO_READ_FILE,schemaPath);
        }
        try {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            HyscaleException ex = new HyscaleException(CommonErrorCode.UNABLE_READ_SCHEMA,schemaPath);
            LOGGER.error(ex.getMessage());
            throw ex;
        }
    }
}
