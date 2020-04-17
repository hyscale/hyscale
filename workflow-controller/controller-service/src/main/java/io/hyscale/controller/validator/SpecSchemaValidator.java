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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
import io.hyscale.schema.validator.SchemaValidator;

public abstract class SpecSchemaValidator implements Validator<File> {

    private static final Logger logger = LoggerFactory.getLogger(SpecSchemaValidator.class);

    @Autowired
    private SchemaValidator schemaValidator;

    @Override
    public boolean validate(File specFile) throws HyscaleException {
        ProcessingReport processingReport;
        try {
            processingReport = schemaValidator.validateSpec(DataFormatConverter.yamlToJson(specFile),
                    getReferenceSchemaType());
        } catch (HyscaleException e) {
            WorkflowLogger.persist(getActivity(), LoggerTags.ERROR, e.getMessage());
            throw new HyscaleException(e.getHyscaleErrorCode(), ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
        }
        if (processingReport.isSuccess()) {
            try {
                return validateData(specFile);
            } catch (HyscaleException e) {
                WorkflowLogger.persist(getActivity(), LoggerTags.ERROR, e.getMessage());
                throw new HyscaleException(e.getHyscaleErrorCode(), ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
            }
        }
        StringBuilder messageBuilder = new StringBuilder();
        Iterator<ProcessingMessage> messageIterator = processingReport.iterator();
        while (messageIterator.hasNext()) {
            ProcessingMessage message = messageIterator.next();
            messageBuilder.append(message.toString());
        }
        WorkflowLogger.persist(getActivity(), LoggerTags.ERROR, messageBuilder.toString());
        logger.error(messageBuilder.toString());
        
        return false;
    }

    protected abstract boolean validateData(File inputFile) throws HyscaleException;

    protected abstract HyscaleSpecType getReferenceSchemaType();
    
    protected abstract Activity getActivity();
}
