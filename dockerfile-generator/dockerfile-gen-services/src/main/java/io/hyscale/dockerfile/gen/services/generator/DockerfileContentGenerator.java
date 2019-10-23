package io.hyscale.dockerfile.gen.services.generator;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.core.models.DockerfileContent;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public interface DockerfileContentGenerator {

	public DockerfileContent generate(ServiceSpec serviceSpec, DockerfileGenContext context) throws HyscaleException;
}
