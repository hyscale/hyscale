package io.hyscale.ctl.builder.services.service;

import io.hyscale.ctl.builder.core.models.BuildContext;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

public interface ImageBuildService {

	public BuildContext build(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException;
}
