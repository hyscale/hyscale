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
package io.hyscale.controller.directive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.directive.impl.PropsJsonHandler;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.PropType;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class PropsUpdateTestHandler implements ServiceSpecUpdateTestHandler {

    private static final String propsUpdateSpec = "/servicespecs/props_update.hspec.yaml";
    
    private static final String JSON_PATH = HyscaleSpecFields.getPath(HyscaleSpecFields.props);
    
    @Autowired
    private PropsJsonHandler propsHandler;
    
    public String getServiceSpec() {
        return propsUpdateSpec;
    }
    
    public ServiceSpec getUpdatedServiceSpec(ObjectNode serviceSpecNode) {
        try {
            propsHandler.update(serviceSpecNode);
            return new ServiceSpec(serviceSpecNode);
        } catch (HyscaleException e) {
            fail();
        }
        return null;
    }
    
    @Override
    public boolean performValidation(ServiceSpec oldServiceSpec, ServiceSpec updatedServiceSpec) {
        
        Map<String, String> oldProps = null;
        Map<String, String> updatedProps = null;
        try {
            TypeReference<Map<String, String>> typeReference = new TypeReference<Map<String, String>>() {
            };
            oldProps = oldServiceSpec.get(JSON_PATH, typeReference);
            updatedProps = updatedServiceSpec.get(JSON_PATH, typeReference);
        } catch (HyscaleException e) {
            fail();
        }

        for (Entry<String, String> each : updatedProps.entrySet()) {
            String value = each.getValue();
            if (PropType.FILE.getPatternMatcher().matcher(value).matches()) {
                assertEquals(WindowsUtil.updateToUnixFileSeparator(oldProps.get(each.getKey())), value);
            } else {
                assertEquals(oldProps.get(each.getKey()), value);
            }
        }
        return true;

    }
    
}
