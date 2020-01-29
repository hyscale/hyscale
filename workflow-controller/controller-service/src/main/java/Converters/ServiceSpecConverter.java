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

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.util.ServiceSpecUtil;
import io.hyscale.servicespec.commons.activity.ServiceSpecActivity;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/*
Provides parameters and funtions such as profile reference schema,Regex for profile file naming,
data validation and respective warning and error messages  for service spec validation.
 */
public class ServiceSpecConverter extends Converter {
    private static final Logger logger = LoggerFactory.getLogger(ServiceSpecConverter.class);

    private static final String SERVICE_SCHEMA_FILE = "/hspec/" + ToolConstants.RELEASE + "/service-spec.json";

    @Override
    public String getFilePattern() {
        return ValidationConstants.SERVICE_SPEC_NAME_REGEX;
    }

    @Override
    public String getReferenceSchema() {
        return SERVICE_SCHEMA_FILE;
    }

    @Override
    public String getWarnMessage() {
        return ValidationConstants.INVALID_SERVICE_SPEC_NAME_MSG;
    }

    @Override
    public void validateData(File serviceSpecFile) throws HyscaleException{
        String serviceFileName = FilenameUtils.getBaseName(serviceSpecFile.getPath());
        String serviceName =  serviceFileName.split("\\.")[0];
        if(!serviceName.equals(ServiceSpecUtil.getServiceName(serviceSpecFile))){
            logger.warn(ServiceSpecActivity.SERVICE_NAME_MISMATCH.getActivityMessage(), serviceFileName);
        }
    }

}
