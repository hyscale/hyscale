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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleError;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.SupportingFile;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.dockerfile.gen.services.exception.DockerfileErrorCodes;
import io.hyscale.dockerfile.gen.services.model.CommandType;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.services.util.ServiceSpecTestUtil;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringBootTest
@TestInstance(Lifecycle.PER_CLASS)
class DockerScriptManagerTest {

    @Autowired
    private DockerScriptManagerImpl dockerScriptManagerImpl;
    
    @Autowired
    private MustacheTemplateResolver mustacheTemplateResolver;
    
    @BeforeEach
    public void initMocks() throws HyscaleException {
        Mockito.when(mustacheTemplateResolver.resolveTemplate(anyString(), anyMap())).thenReturn("test");
    }
    
    public static Stream<Arguments> input() {
        return Stream.of(
                Arguments.of(null, false, CommonErrorCode.SERVICE_SPEC_REQUIRED),
                Arguments.of("/input/command-script/script-doesnot-exist.hspec", false, DockerfileErrorCodes.SCRIPT_FILE_NOT_FOUND),
                Arguments.of("/input/command-script/script-and-cmd.hspec", true, null),
                Arguments.of("/input/command-script/config-script.hspec", true, null),
                Arguments.of("/input/command-script/run-script.hspec", true, null),
                Arguments.of("/input/command-script/run-cmd.hspec", true, null),
                Arguments.of("/input/command-script/config-cmd.hspec", true, null),
                Arguments.of("/input/command-script/noscript.hspec", false, null));
    }

    @ParameterizedTest
    @MethodSource("input")
    void scriptManagerTest(String serviceSpecPath, boolean supportingFilesAvailable, HyscaleError hyscaleError) {
        try {
            ServiceSpec serviceSpec = ServiceSpecTestUtil.getServiceSpec(serviceSpecPath, true);
            List<SupportingFile> supportingFiles = dockerScriptManagerImpl.getSupportingFiles(serviceSpec,
                    new DockerfileGenContext());
            if (hyscaleError != null) {
                fail("Expected error: " + hyscaleError);
            }
            assertTrue(supportingFilesAvailable
                    ? !supportingFiles.isEmpty() && supportingFiles.stream().allMatch(each -> verify(each))
                    : CollectionUtils.isEmpty(supportingFiles));
        } catch (HyscaleException e) {
            if (hyscaleError == null || !hyscaleError.equals(e.getHyscaleError())) {
                fail(e);
            }
        }
    }
    
    @Test
    void testScript() throws HyscaleException {
        try {
            assertTrue(StringUtils.isNotBlank(dockerScriptManagerImpl.getScript(CommandType.CONFIGURE, "test command")));
            assertNull(dockerScriptManagerImpl.getScript(CommandType.CONFIGURE, null));
        } catch (HyscaleException e) {
            fail(e);
        }
    }
    
    private boolean verify(SupportingFile supportingFile) {
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
