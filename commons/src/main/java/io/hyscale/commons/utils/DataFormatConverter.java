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
package io.hyscale.commons.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class DataFormatConverter {
    /**
     * Converts the given String to Json.
     *
     * @param yaml String input
     * @return Json String
     * @throws HyscaleException if any IO Exception occurs while conversion.
     */
    public static String yamlToJson(String yaml) throws HyscaleException {
        if(StringUtils.isBlank(yaml)){
            return yaml;
        }
        ObjectMapper yamlReader = ObjectMapperFactory.yamlMapper();
        Object obj;
        try {
            obj = yamlReader.readValue(yaml, Object.class);
            ObjectMapper jsonWriter = ObjectMapperFactory.jsonMapper();
            return jsonWriter.writeValueAsString(obj);
        } catch (IOException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.YAML_TO_JSON_CONVERSION_FAILURE,e.getMessage());
            throw ex;
        }
    }
}
