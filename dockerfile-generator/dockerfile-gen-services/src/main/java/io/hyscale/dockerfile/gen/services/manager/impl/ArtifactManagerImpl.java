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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.dockerfile.gen.services.manager.DockerfileEntityManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.Artifact;
import io.hyscale.commons.models.DecoratedArrayList;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.commons.models.SupportingFile;
import io.hyscale.dockerfile.gen.services.config.DockerfileGenConfig;
import io.hyscale.dockerfile.gen.services.exception.DockerfileErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class ArtifactManagerImpl implements DockerfileEntityManager {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactManagerImpl.class);

    @Autowired
    private DockerfileGenConfig dockerfileGenConfig;

    @Override
    public List<SupportingFile> getSupportingFiles(ServiceSpec serviceSpec, DockerfileGenContext context)
            throws HyscaleException {
        List<SupportingFile> supportingFiles = new ArrayList<SupportingFile>();
        TypeReference<List<Artifact>> artifactTypeRef = new TypeReference<List<Artifact>>() {
        };
        List<Artifact> artifactsList;
        try {
            artifactsList = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image,
                    HyscaleSpecFields.buildSpec, HyscaleSpecFields.artifacts), artifactTypeRef);
        } catch (HyscaleException e) {
            logger.error("Error while getting artifacts, {}", e.toString());
            throw e;
        }

        if (artifactsList == null || artifactsList.isEmpty()) {
            return supportingFiles;
        }
        /*
         * Artifacts -> Supporting file, Source -> File, Name -> relativePathDir
         */
        validate(artifactsList);
        artifactsList.stream().forEach(each -> {
            SupportingFile supportingFile = new SupportingFile();
            File artifactFile = new File(SetupConfig.getAbsolutePath(each.getSource()));
            String relativePath = dockerfileGenConfig.getRelativeArtifactDir(each.getName());
            supportingFile.setFile(artifactFile);
            supportingFile.setRelativePath(relativePath);
            supportingFiles.add(supportingFile);
        });
        return supportingFiles;
    }

    private void validate(List<Artifact> artifactsList) throws HyscaleException {
        if (artifactsList == null || artifactsList.isEmpty()) {
            return;
        }
        // Artifacts should exist
        List<String> artifactsNotFound = new ArrayList<String>();
        artifactsList.stream().forEach(artifact -> {
            String artifactPath = SetupConfig.getAbsolutePath(artifact.getSource());
            File artifactFile = new File(artifactPath);
            if (!artifactFile.exists() || !artifactFile.isFile()) {
                artifactsNotFound.add(artifactPath);
            }
        });
        if (!artifactsNotFound.isEmpty()) {
            throw new HyscaleException(DockerfileErrorCodes.ARTIFACTS_NOT_FOUND, artifactsNotFound.toString());
        }
    }

    // Update source for dockerfile
    public DecoratedArrayList<Artifact> getUpdatedArtifacts(List<Artifact> artifactsList) {
        if (artifactsList == null) {
            return null;
        }
        DecoratedArrayList<Artifact> updatedArtifacts = new DecoratedArrayList<Artifact>();
        artifactsList.stream().filter(each -> {
            return StringUtils.isNotBlank(each.getName()) && StringUtils.isNotBlank(each.getSource()) && StringUtils.isNotBlank(each.getDestination());
        }).forEach(each -> {
            Artifact artifact = new Artifact();
            artifact.setName(each.getName());
            artifact.setDestination(each.getDestination());
            String newSource = dockerfileGenConfig.getRelativeArtifactDir(each.getName())
                    + new File(each.getSource()).getName();
            artifact.setSource(newSource);
            updatedArtifacts.add(artifact);
        });
        return updatedArtifacts;
    }

}
