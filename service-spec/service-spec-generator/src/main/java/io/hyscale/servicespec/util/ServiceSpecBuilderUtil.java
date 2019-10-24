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
package io.hyscale.servicespec.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;

public class ServiceSpecBuilderUtil {

	public static String yamlToJson(String yaml) throws HyscaleException {
		ObjectMapper yamlReader = ObjectMapperFactory.yamlMapper();
		Object obj;
		try {
			obj = yamlReader.readValue(yaml, Object.class);
			ObjectMapper jsonWriter = ObjectMapperFactory.jsonMapper();

			return jsonWriter.writeValueAsString(obj);
		} catch (IOException e) {
			HyscaleException ex = new HyscaleException(e, ServiceSpecErrorCodes.CANNOT_PROCESS_SERVICE_SPEC);
			throw ex;
		}
	}

	public static String updateProfile(String profileSpecification) {

		DocumentContext doc = JsonPath.parse(profileSpecification);
		// Environment information
		doc.delete(HyscaleSpecFields.getPath(HyscaleSpecFields.environment));
		// Override information
		doc.delete(HyscaleSpecFields.getPath(HyscaleSpecFields.overrides));

		return doc.jsonString();
	}

}
