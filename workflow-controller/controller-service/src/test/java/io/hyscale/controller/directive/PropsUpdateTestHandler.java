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

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.directive.impl.PropsJsonHandler;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.PropType;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class PropsUpdateTestHandler implements IServiceSpecUpdateTestHandler<Map<String, String>> {

    private static final String propsUpdateSpec = "/servicespecs/props_update.hspec";
    
    private static final String JSON_PATH = HyscaleSpecFields.getPath(HyscaleSpecFields.props);
    
    @Autowired
    private PropsJsonHandler propsHandler;
    
    @Override
    public String getServiceSpecPath() {
        return propsUpdateSpec;
    }
    
    @Override
    public ServiceSpec updateServiceSpec(ObjectNode serviceSpecNode) {
        try {
            propsHandler.update(serviceSpecNode);
            return new ServiceSpec(serviceSpecNode);
        } catch (HyscaleException e) {
            fail();
        }
        return null;
    }
    
    @Override
    public boolean validate(Map<String, String> oldProps, Map<String, String> updatedProps) {
        
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

    @Override
    public String getJsonPath() {
        return JSON_PATH;
    }

    @Override
    public Class<Map<String, String>> getType() {
        return (Class<Map<String, String>>) (Class) Map.class;
    }
    
}
