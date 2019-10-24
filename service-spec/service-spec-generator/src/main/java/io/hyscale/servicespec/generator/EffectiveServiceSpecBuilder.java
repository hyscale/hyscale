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
package io.hyscale.servicespec.generator;

import java.io.IOException;

import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.model.service.ServiceSpecification;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.model.AnnotationFieldDataProvider;
import io.hyscale.servicespec.model.Type;
import io.hyscale.servicespec.util.ServiceSpecBuilderUtil;
import io.hyscale.servicespec.util.StrategicPatch;

/**
 * Builder to create effective service spec by merging service spec and service profile
 * @author tushart
 *
 */
public class EffectiveServiceSpecBuilder {

	private Type type = Type.YAML;

	private String serviceSpec;

	private String profile;

	public EffectiveServiceSpecBuilder type(Type type) {
		this.type = type;
		return this;
	}

	public EffectiveServiceSpecBuilder withServiceSpec(String serviceSpec) {
		this.serviceSpec = serviceSpec;
		return this;
	}

	public EffectiveServiceSpecBuilder withProfile(String profile) {
		this.profile = profile;
		return this;
	}

	public String build() throws HyscaleException {

		validate(serviceSpec, profile);

		if (this.type == Type.YAML) {
			// convert to JSON
			serviceSpec = ServiceSpecBuilderUtil.yamlToJson(serviceSpec);
			profile = ServiceSpecBuilderUtil.yamlToJson(profile);
		}

		// Remove unrequired fields from the profile
		profile = ServiceSpecBuilderUtil.updateProfile(profile);

		AnnotationFieldDataProvider fieldDataProvider = new AnnotationFieldDataProvider(ServiceSpecification.class);
		String strategicMergeJson = StrategicPatch.apply(serviceSpec, profile, fieldDataProvider);

		return strategicMergeJson;
	}

	private void validate(String serviceSpec, String profile) throws HyscaleException {
		if (StringUtils.isBlank(serviceSpec)) {
			throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_PARSE_ERROR);
		}
		if (StringUtils.isBlank(profile)) {
			throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_PROFILE_PARSE_ERROR);
		}
		ObjectMapper mapper;
		if (Type.JSON == this.type) {
			mapper = ObjectMapperFactory.jsonMapper();
		} else {
			mapper = ObjectMapperFactory.yamlMapper();
		}
		try {
			mapper.readTree(serviceSpec);
		} catch (IOException e) {
			throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_PARSE_ERROR);
		}
		try {
			mapper.readTree(profile);
		} catch (IOException e) {
			throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_PROFILE_PARSE_ERROR);
		}
	}

}
