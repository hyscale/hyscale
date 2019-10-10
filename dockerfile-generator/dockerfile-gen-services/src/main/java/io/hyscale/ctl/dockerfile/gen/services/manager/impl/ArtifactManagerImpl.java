package io.hyscale.ctl.dockerfile.gen.services.manager.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.hyscale.ctl.commons.config.SetupConfig;
import io.hyscale.ctl.dockerfile.gen.services.manager.DockerfileEntityManager;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.servicespec.commons.model.service.Artifact;
import io.hyscale.ctl.commons.models.DecoratedArrayList;
import io.hyscale.ctl.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.ctl.commons.models.SupportingFile;
import io.hyscale.ctl.dockerfile.gen.services.config.DockerfileGenConfig;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

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

        if (artifactsList == null) {
            return supportingFiles;
        }
        /*
         * Artifacts -> Supporting file, Source -> File, Name -> relativePathDir
         */
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
