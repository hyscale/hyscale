package io.hyscale.dockerfile.gen.services.manager;

import java.util.List;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.commons.models.SupportingFile;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public interface DockerfileEntityManager {

	public List<SupportingFile> getSupportingFiles(ServiceSpec serviceSpec, DockerfileGenContext context)
			throws HyscaleException;

}
