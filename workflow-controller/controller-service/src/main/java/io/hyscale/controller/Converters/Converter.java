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
package io.hyscale.controller.Converters;

import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.HyscaleSpecType;
import io.hyscale.commons.utils.DataFormatConverter;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.schema.validator.SchemaValidator;
import io.hyscale.servicespec.commons.activity.ServiceSpecActivity;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Converter class implements CommandLine framework ITypeConverter ,
 * performs command line input validation and assigns to the respective field.
 */
public abstract class Converter implements CommandLine.ITypeConverter<List<File>> {

    private static final Logger logger = LoggerFactory.getLogger(Converter.class);

    @Autowired
    private SchemaValidator schemaValidator;

    public abstract String getFilePattern();

    public abstract HyscaleSpecType getReferenceSchemaType();

    public abstract ServiceSpecActivity getWarnMessage();

    public abstract boolean validateData(File inputFile) throws HyscaleException;

    /**
     * Performs command line input validation and assigns to the respective field.
     *
     * <ul>
     *  <li>if input found empty throws Empty file path Hyscale Exception.</li>
     *  <li>if input file does not exits throws File not found Hyscale Exception.</li>
     *  <li>if input is not file throws Cannot process file Hyscale Exception.</li>
     *  <li>if input file is empty throws Empty file Hyscale Exception.</li>
     *  <li>if input does not satisfy the respective reference schema throws Invalid Format Hyscale Exception.</li>
     *  </ul>
     * @param inputFilePath
     * @return assigns the file to field if valid.
     * @throws Exception contains wrapped HyscaleExcpetion for Handling.
     */
    @Override
    public List<File> convert(String inputFilePath) throws Exception {
        ProcessingReport processingReport;
        List<File> inputFiles = new ArrayList<File>();

        if (StringUtils.isBlank(inputFilePath)) {
            WorkflowLogger.error(ControllerActivity.EMPTY_FILE_PATH);
            throw new HyscaleException(CommonErrorCode.EMPTY_FILE_PATH, ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
        }

        File specfile = new File(inputFilePath);
        if (!specfile.exists()) {
            WorkflowLogger.error(ControllerActivity.CANNOT_FIND_FILE, inputFilePath);
            throw new HyscaleException(CommonErrorCode.FILE_NOT_FOUND,ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE,inputFilePath);
        }
        if (specfile.isDirectory()) {
            WorkflowLogger.error(ControllerActivity.DIRECTORY_INPUT_FOUND, inputFilePath);
            throw new HyscaleException(CommonErrorCode.FOUND_DIRECTORY_INSTEAD_OF_FILE,ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE,inputFilePath);
        }
        if (!specfile.isFile()) {
            WorkflowLogger.error(ControllerActivity.INVALID_FILE_INPUT, inputFilePath);
            throw new HyscaleException(CommonErrorCode.INVALID_FILE_INPUT,ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
        }
        String fileName = specfile.getName();
        if (!fileName.matches(getFilePattern())) {
            WorkflowLogger.persist(getWarnMessage(),fileName);
            logger.warn(getWarnMessage().getActivityMessage(), fileName);
        }
        if (specfile.length() == 0) {
            WorkflowLogger.error(ControllerActivity.EMPTY_FILE_FOUND, inputFilePath);
            throw new HyscaleException(CommonErrorCode.EMPTY_FILE_FOUND,ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE, inputFilePath);
        }
        try {
            processingReport = schemaValidator.validateSpec(DataFormatConverter.yamlToJson(specfile), getReferenceSchemaType());
        } catch (HyscaleException e) {
            WorkflowLogger.error(ServiceSpecActivity.ERROR, e.getMessage());
            throw new HyscaleException(e.getHyscaleErrorCode(),ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
        }
        if (processingReport.isSuccess()) {
            try {
                validateData(specfile);
            }catch (HyscaleException e) {
                WorkflowLogger.error(ServiceSpecActivity.ERROR, e.getMessage());
                throw new HyscaleException(e.getHyscaleErrorCode(),ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE);
            }
            inputFiles.add(specfile);
            return inputFiles;
        }
        StringBuilder messageBuilder = new StringBuilder();
        Iterator<ProcessingMessage> messageIterator = processingReport.iterator();
        while (messageIterator.hasNext()) {
            ProcessingMessage message = messageIterator.next();
            messageBuilder.append(message.toString());
        }
        WorkflowLogger.error(ServiceSpecActivity.SCHEMA_VALIDATION_ERROR,specfile.getPath(),messageBuilder.toString());
        logger.error(messageBuilder.toString());
        throw new HyscaleException(ServiceSpecErrorCodes.INVALID_FORMAT, ToolConstants.SCHEMA_VALIDATION_FAILURE_ERROR_CODE, messageBuilder.toString());
    }
}

