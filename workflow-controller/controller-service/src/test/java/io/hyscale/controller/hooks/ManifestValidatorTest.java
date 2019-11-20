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
package io.hyscale.controller.hooks;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class ManifestValidatorTest {

    private static ManifestValidatorHook manifestValidatorHook = new ManifestValidatorHook();

    @Test
    public void nullServiceSpec() {
        WorkflowContext context = new WorkflowContext();
        assertThrows(HyscaleException.class, () -> manifestValidatorHook.preHook(context));
    }

    @Test
    public void validManifest() {
        WorkflowContext context = new WorkflowContext();
        try {
            context.setServiceSpec(getServiceSpec("/servicespecs/myservice.hspec.yaml"));
        } catch (IOException e) {
            fail();
        }
        try {
            manifestValidatorHook.preHook(context);
        } catch (HyscaleException e) {
            fail();
        }
    }

    @Test
    public void invalidVolumes() {
        WorkflowContext context = new WorkflowContext();
        try {
            context.setServiceSpec(getServiceSpec("/servicespecs/invalid_vol.hspec.yaml"));
        } catch (IOException e) {
            fail();
        }
        assertThrows(HyscaleException.class, () -> manifestValidatorHook.preHook(context));
    }

    @Test
    public void invalidPorts() {
        WorkflowContext context = new WorkflowContext();
        try {
            context.setServiceSpec(getServiceSpec("/servicespecs/invalid_ports.hspec.yaml"));
        } catch (IOException e) {
            fail();
        }
        assertThrows(HyscaleException.class, () -> manifestValidatorHook.preHook(context));
    }

    private ServiceSpec getServiceSpec(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        InputStream resourceAsStream = ManifestValidatorTest.class.getResourceAsStream(filePath);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        ObjectNode rootNode = (ObjectNode) objectMapper.readTree(testData);
        return new ServiceSpec(rootNode);
    }
}
