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
package io.hyscale.dockerfile.gen.services.manager.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.SupportingFile;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.services.util.ServiceSpecTestUtil;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringBootTest
public class DockerScriptManagerTest {

    @Autowired
    private DockerScriptManagerImpl dockerScriptManagerImpl;

    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of("/input/command-script/script-and-cmd.hspec"),
                Arguments.of("/input/command-script/config-script.hspec"),
                Arguments.of("/input/command-script/run-script.hspec"),
                Arguments.of("/input/command-script/run-cmd.hspec"),
                Arguments.of("/input/command-script/config-cmd.hspec"));
    }

    @ParameterizedTest
    @MethodSource("input")
    public void scriptManagerTest(String serviceSpecPath) throws IOException {
        ServiceSpec serviceSpec = ServiceSpecTestUtil.getServiceSpec(serviceSpecPath);
        try {
            List<SupportingFile> supportingFiles = dockerScriptManagerImpl.getSupportingFiles(serviceSpec,
                    new DockerfileGenContext());
            supportingFiles.stream().forEach(each -> {
                if (!isFileValid(each)) {
                    fail();
                }
            });
        } catch (HyscaleException e) {
            fail(e);
        }
    }

    @Test
    public void noScriptTest() throws IOException {
        ServiceSpec serviceSpec = ServiceSpecTestUtil.getServiceSpec("/input/command-script/noscript.hspec");
        try {
            List<SupportingFile> supportingFiles = dockerScriptManagerImpl.getSupportingFiles(serviceSpec,
                    new DockerfileGenContext());
            assertTrue(CollectionUtils.isEmpty(supportingFiles));
        } catch (HyscaleException e) {
            fail(e);
        }
    }

    private boolean isFileValid(SupportingFile supportingFile) {
        if (supportingFile == null) {
            return false;
        }
        if (supportingFile.getFile() == null && supportingFile.getFileSpec() == null) {
            return false;
        }

        if (supportingFile.getFile() != null && !supportingFile.getFile().exists()) {
            return false;
        }

        if (supportingFile.getFileSpec() != null && (StringUtils.isBlank(supportingFile.getFileSpec().getContent())
                || StringUtils.isBlank(supportingFile.getFileSpec().getName()))) {
            return false;
        }
        return true;
    }
}
