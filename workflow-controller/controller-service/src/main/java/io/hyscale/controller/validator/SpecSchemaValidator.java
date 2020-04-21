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
package io.hyscale.controller.validator;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.LoggerTags;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Activity;
import io.hyscale.commons.models.HyscaleSpecType;
import io.hyscale.commons.utils.DataFormatConverter;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.validator.impl.ProfileSpecSchemaValidator;
import io.hyscale.controller.validator.impl.ServiceSpecSchemaValidator;
import io.hyscale.schema.validator.SchemaValidator;

/**
 * Parent level validator to validate schema such as
 * {@link ServiceSpecSchemaValidator} , {@link ProfileSpecSchemaValidator}
 *  
 */
public abstract class SpecSchemaValidator implements Validator<File> {

    private static final Logger logger = LoggerFactory.getLogger(SpecSchemaValidator.class);
    
    private static final String ERROR_MESSAGE = "Incorrect yaml format";
    
    @Autowired
    private SchemaValidator schemaValidator;

    @Override
    public boolean validate(File specFile) throws HyscaleException {
        String fileName = specFile.getName();
        ProcessingReport processingReport;
        String jsonData = null;
        try {
            jsonData = DataFormatConverter.yamlToJson(specFile);
        } catch (HyscaleException e) {
            WorkflowLogger.persist(getActivity(), LoggerTags.ERROR, fileName, ERROR_MESSAGE);
            throw new HyscaleException(e.getHyscaleErrorCode(), ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
        }
        try {
            processingReport = schemaValidator.validateSpec(jsonData,
                    getReferenceSchemaType());
        } catch (HyscaleException e) {
            WorkflowLogger.persist(getActivity(), LoggerTags.ERROR, fileName, e.getMessage());
            throw new HyscaleException(e.getHyscaleErrorCode(), ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
        }
        if (processingReport.isSuccess()) {
            try {
                return validateData(specFile);
            } catch (HyscaleException e) {
                WorkflowLogger.persist(getActivity(), LoggerTags.ERROR, fileName, e.getMessage());
                throw new HyscaleException(e.getHyscaleErrorCode(), ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
            }
        }
        String errorMessage = getErrorMessage(processingReport);
        
        WorkflowLogger.persist(getActivity(), LoggerTags.ERROR, fileName, errorMessage);
        logger.error(errorMessage);
        
        return false;
    }
    
    /**
     * 
     * @param processingReport
     * @return user friendly error message
     */
    private String getErrorMessage(ProcessingReport processingReport) {
        StringBuilder messageBuilder = new StringBuilder();
        Iterator<ProcessingMessage> messageIterator = processingReport.iterator();
        while (messageIterator.hasNext()) {
            ProcessingMessage message = messageIterator.next();
            if (StringUtils.isNotBlank(message.getMessage())) {
                messageBuilder.append(message.getMessage());
                JsonNode jsonNode = message.asJson();
                String location = getLocation(jsonNode);
                if (location != null) {
                    messageBuilder.append(" at ").append(location);
                }
            } else {
                messageBuilder.append(message.toString());
            }
            messageBuilder.append("\n");
        }
        return messageBuilder.toString();
    }

    private String getLocation(JsonNode message) {
        if (message == null) {
            return null;
        }
        JsonNode instance = message.get("instance");
        if (instance == null) {
            return null;
        }
        JsonNode pointer = instance.get("pointer");
        if (pointer == null) {
            return null;
        }
        return pointer.asText();
    }

    protected abstract boolean validateData(File inputFile) throws HyscaleException;

    protected abstract HyscaleSpecType getReferenceSchemaType();
    
    protected abstract Activity getActivity();
}
