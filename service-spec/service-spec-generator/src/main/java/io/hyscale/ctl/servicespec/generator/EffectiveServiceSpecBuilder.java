package io.hyscale.ctl.servicespec.generator;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import io.hyscale.ctl.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpecification;
import io.hyscale.ctl.servicespec.model.AnnotationFieldDataProvider;
import io.hyscale.ctl.servicespec.model.Type;
import io.hyscale.ctl.servicespec.util.ServiceSpecBuilderUtil;
import io.hyscale.ctl.servicespec.util.StrategicPatch;

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
