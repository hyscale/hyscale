package io.hyscale.dockerfile.gen.services.persist;

import java.util.List;

import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.core.models.DockerfileContent;
import io.hyscale.commons.models.SupportingFile;

public abstract class DockerfilePersistenceService {

	public boolean persistDockerfiles(DockerfileContent dockerfileContent, List<SupportingFile> supportingFiles,
			DockerfileGenContext context) {
		if (context.isSkipCopy()) {
			return persist(dockerfileContent, context);
		} else {
			return copySupportingFiles(supportingFiles, context) && persist(dockerfileContent, context);
		}
	}

	protected abstract boolean copySupportingFiles(List<SupportingFile> supportingFiles, DockerfileGenContext context);

	protected abstract boolean persist(DockerfileContent dockerfileContent, DockerfileGenContext context);

}
