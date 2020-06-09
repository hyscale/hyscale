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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.SupportingFile;
import io.hyscale.dockerfile.gen.services.config.DockerfileGenConfig;
import io.hyscale.dockerfile.gen.services.exception.DockerfileErrorCodes;
import io.hyscale.dockerfile.gen.services.util.ServiceSpecTestUtil;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Artifact;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringBootTest
public class ArtifactManagerTest {

    @Autowired
    private ArtifactManagerImpl artifactManagerImpl;

    @Autowired
    private DockerfileGenConfig dockerfileGenConfig;

    private TypeReference<List<Artifact>> artifactTypeRef = new TypeReference<List<Artifact>>() {
    };

    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of("/input/artifacts/valid-artifact.hspec", null),
                Arguments.of("/input/artifacts/artifact-doesnt-exist.hspec", new HyscaleException(DockerfileErrorCodes.ARTIFACTS_NOT_FOUND)),
                Arguments.of("/input/artifacts/no-artifact.hspec", null));
    }

    @ParameterizedTest
    @MethodSource("input")
    public void artifactManagetTest(String serviceSpecPath, HyscaleException expectedException) throws IOException {
        ServiceSpec serviceSpec = ServiceSpecTestUtil.getServiceSpec(serviceSpecPath);
        List<SupportingFile> supportingFiles = null;
        try {
            supportingFiles = artifactManagerImpl.getSupportingFiles(serviceSpec, null);
            if (!isFileValid(supportingFiles, serviceSpec)) {
                fail();
            }
        } catch (HyscaleException e) {
            if (expectedException == null || !e.getHyscaleError().equals(expectedException.getHyscaleError())) {
                fail(e);
            }
        }
    }

    private boolean isFileValid(List<SupportingFile> supportingFiles, ServiceSpec serviceSpec) {
        List<Artifact> artifactsList = null;
        try {
            artifactsList = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image,
                    HyscaleSpecFields.buildSpec, HyscaleSpecFields.artifacts), artifactTypeRef);
        } catch (HyscaleException e) {
        }
        if ((supportingFiles == null || supportingFiles.isEmpty())
                && (artifactsList == null || artifactsList.isEmpty())) {
            return true;
        }

        // For each artifact there should be one supporting file
        return artifactsList.stream().allMatch(artifact -> {
            String relativePath = dockerfileGenConfig.getRelativeArtifactDir(artifact.getName());
            return supportingFiles.stream().anyMatch(supportingFile -> {
                return supportingFile.getRelativePath().equalsIgnoreCase(relativePath);
            });
        });
    }

}
