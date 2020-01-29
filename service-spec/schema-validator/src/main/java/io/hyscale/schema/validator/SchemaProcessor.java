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
import io.hyscale.commons.utils.DataFormatConverter;
import io.hyscale.servicespec.commons.builder.ServiceInputType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Processes given input from Yaml to Json and returns validated ProcessingReport from JsonSchemaValidator.
 */
public class SchemaProcessor {

    /**
     * Converts given String to json if type is yaml and validates whether the converted satisfies the reference schema.
     *
     * @param spec
     * @param referenceSchema
     * @param type
     * @return ProcessingReport
     * @throws HyscaleException
     */
    public static ProcessingReport validateSpec(String spec, String referenceSchema, ServiceInputType type) throws HyscaleException{
        if (type == ServiceInputType.YAML) {
            spec = DataFormatConverter.yamlToJson(spec);
        }
        return validateSpec(spec, referenceSchema);
    }

    /**
     * Converts given File to json if type is yaml and validates whether the converted satisfies the reference schema.
     *
     * @param specFile
     * @param referenceSchema
     * @param type
     * @return ProcessingReport
     * @throws HyscaleException
     */
    public static ProcessingReport validateSpec(File specFile, String referenceSchema, ServiceInputType type) throws HyscaleException {
        try {
            String serviceSpec = FileUtils.readFileToString(specFile, ToolConstants.CHARACTER_ENCODING);
            return validateSpec(serviceSpec, referenceSchema, type);
        } catch (IOException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.FAILED_TO_READ_FILE, e.getMessage());
            throw ex;
        }
    }


    private static ProcessingReport validateSpec(String serviceSpec, String referenceSchema) throws HyscaleException{
        return JsonSchemaValidator.validate(serviceSpec, referenceSchema);
    }
}
