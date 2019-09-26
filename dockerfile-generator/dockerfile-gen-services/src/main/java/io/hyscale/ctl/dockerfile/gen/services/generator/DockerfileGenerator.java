package io.hyscale.ctl.dockerfile.gen.services.generator;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.DockerfileEntity;
import io.hyscale.ctl.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

public interface DockerfileGenerator {

	DockerfileEntity generateDockerfile(ServiceSpec serviceSpec, DockerfileGenContext context) throws HyscaleException;
}
