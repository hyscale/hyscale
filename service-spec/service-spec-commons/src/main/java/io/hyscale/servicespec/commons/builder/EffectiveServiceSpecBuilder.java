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
package io.hyscale.servicespec.commons.builder;


import java.io.IOException;

import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.utils.DataFormatConverter;
import io.hyscale.servicespec.commons.activity.ServiceSpecActivity;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.framework.patch.FieldMetaDataProvider;
import io.hyscale.commons.framework.patch.StrategicPatch;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;

/**
 * Builder to create effective service spec by merging service spec and service profile
 * @author tushar
 *
 */
public class EffectiveServiceSpecBuilder {

    private ServiceInputType serviceInputType = ServiceInputType.YAML;

    private String serviceSpec;

    private String profile;

    private FieldMetaDataProvider fieldMetaDataProvider;

    public EffectiveServiceSpecBuilder type(ServiceInputType serviceInputType) {
        this.serviceInputType = serviceInputType;
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

    public EffectiveServiceSpecBuilder withFieldMetaDataProvider(FieldMetaDataProvider fieldMetaDataProvider) {
        this.fieldMetaDataProvider = fieldMetaDataProvider;
        return this;
    }

    public String build() throws HyscaleException {

        validate(serviceSpec, profile);

        if (this.serviceInputType == ServiceInputType.YAML) {
            // convert to JSON
            serviceSpec = DataFormatConverter.yamlToJson(serviceSpec);
            profile = DataFormatConverter.yamlToJson(profile);
        }

        // Remove unrequired fields from the profile
        profile = ServiceSpecBuilderUtil.updateProfile(profile);

        return StrategicPatch.apply(serviceSpec, profile, fieldMetaDataProvider);

    }

    //TODO   integrating service spec and profile schema validation
    private void validate(String serviceSpec, String profile) throws HyscaleException {
        if (StringUtils.isBlank(serviceSpec)) {
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_REQUIRED);
        }
        if (StringUtils.isBlank(profile)) {
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_PROFILE_REQUIRED);
        }
        ObjectMapper mapper;
        if (ServiceInputType.JSON == this.serviceInputType) {
            mapper = ObjectMapperFactory.jsonMapper();
        } else {
            mapper = ObjectMapperFactory.yamlMapper();
        }
        try {
            mapper.readTree(serviceSpec);
        } catch (IOException e) {
            WorkflowLogger.error(ServiceSpecActivity.ERROR, e.getMessage());
            throw new HyscaleException(e,ServiceSpecErrorCodes.SERVICE_SPEC_PARSE_ERROR);
        }
        try {
            mapper.readTree(profile);
        } catch (IOException e) {
            WorkflowLogger.error(ServiceSpecActivity.ERROR, e.getMessage());
            throw new HyscaleException(e,ServiceSpecErrorCodes.SERVICE_PROFILE_PARSE_ERROR);
        }
    }

}
