package io.hyscale.ctl.servicespec.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import io.hyscale.ctl.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;

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
