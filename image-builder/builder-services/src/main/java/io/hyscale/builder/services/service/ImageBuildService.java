package io.hyscale.builder.services.service;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public interface ImageBuildService {

	public BuildContext build(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException;
}
