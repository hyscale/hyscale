package io.hyscale.ctl.dockerfile.gen.services.generator;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.ctl.dockerfile.gen.core.models.DockerfileContent;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

public interface DockerfileContentGenerator {

	public DockerfileContent generate(ServiceSpec serviceSpec, DockerfileGenContext context) throws HyscaleException;
}
