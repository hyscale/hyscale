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

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.directive.impl.PropsJsonHandler;
import io.hyscale.controller.util.ServiceSpecTestUtil;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.PropType;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class PropsJsonHandlerTest {

    private static final String FILEPATH = "/servicespecs/props_update.hspec.yaml";

    private static final String JSON_PATH = HyscaleSpecFields.getPath(HyscaleSpecFields.props);

    private static PropsJsonHandler propsHandler = new PropsJsonHandler();

    private static ObjectNode serviceSpecNode = null;

    private static ServiceSpec oldServiceSpec = null;

    private static ServiceSpec updatedServiceSpec = null;

    @BeforeAll
    public static void beforeAll() {
        try {
            oldServiceSpec = ServiceSpecTestUtil.getServiceSpec(FILEPATH);
            serviceSpecNode = (ObjectNode) ServiceSpecTestUtil.getServiceSpecJsonNode(FILEPATH);
            propsHandler.update(serviceSpecNode);
            updatedServiceSpec = new ServiceSpec(serviceSpecNode);
        } catch (IOException | HyscaleException e) {
            fail();
        }
    }

    @Test
    public void updateProps() {
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

    }
}
