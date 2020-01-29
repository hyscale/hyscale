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
package Converters;

import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.schema.validator.SchemaProcessor;
import io.hyscale.servicespec.commons.activity.ServiceSpecActivity;
import io.hyscale.servicespec.commons.builder.ServiceInputType;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public abstract String getFilePattern();

    public abstract String getReferenceSchema();

    public abstract String getWarnMessage();

    public abstract void validateData(File inputFile) throws HyscaleException;

    /**
     *  performs command line input validation and assigns to the respective field.
     *
     *  1.if input found empty throws Empty file path Hyscale Exception.
     *  2.if input file does not exits throws File not found Hyscale Exception.
     *  3.if input is not file throws Cannot process file Hyscale Exception.
     *  4.if input file is empty throws Empty file Hyscale Exception.
     *  5.if input does not satisfy the respective reference schema throws Invalid Format Hyscale Exception.
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
            throw new HyscaleException(CommonErrorCode.EMPTY_FILE_PATH);
        }

        File file = new File(inputFilePath);
        if (!file.exists()) {
            WorkflowLogger.error(ControllerActivity.CANNOT_FIND_FILE, inputFilePath);
            throw new HyscaleException(CommonErrorCode.FILE_NOT_FOUND);
        }
        if (file.isFile()) {
            String fileName = file.getName();
            if (!fileName.matches(getFilePattern())) {
                logger.warn(getWarnMessage(), fileName);
            }
            if (file.length() == 0) {
                WorkflowLogger.error(ControllerActivity.EMPTY_FILE_FOUND, inputFilePath);
                throw new HyscaleException(CommonErrorCode.EMPTY_FILE_FOUND, inputFilePath);
            }
            try {
                validateData(file);
                processingReport = SchemaProcessor.validateSpec(file, getReferenceSchema(), ServiceInputType.YAML);
            } catch (HyscaleException e) {
                WorkflowLogger.error(ServiceSpecActivity.ERROR, e.getMessage());
                throw e;
            }
            if (processingReport.isSuccess()) {
                inputFiles.add(file);
                return inputFiles;
            }
            StringBuilder messageBuilder = new StringBuilder();
            StringBuilder exceptionBuilder = new StringBuilder();
            Iterator<ProcessingMessage> messageIterator = processingReport.iterator();
            while (messageIterator.hasNext()) {
                ProcessingMessage message = messageIterator.next();
                messageBuilder.append(message.getMessage());
                exceptionBuilder.append(message.asException().fillInStackTrace());
            }
            WorkflowLogger.error(ServiceSpecActivity.ERROR, messageBuilder.toString());
            logger.error(messageBuilder.toString());
            logger.error(exceptionBuilder.toString());
            throw new HyscaleException(ServiceSpecErrorCodes.INVALID_FORMAT, messageBuilder.toString());
        }
        if (file.isDirectory()) {
            WorkflowLogger.error(ControllerActivity.DIRECTORY_INPUT_FOUND, inputFilePath);
            throw new HyscaleException(CommonErrorCode.FOUND_DIRECTORY_INSTEAD_OF_FILE, inputFilePath);
        }
        WorkflowLogger.error(ControllerActivity.UNABLE_TO_PROCESS_INPUT, inputFilePath);
        throw new HyscaleException(CommonErrorCode.CANNOT_PROCESS_INPUT);
    }

}

