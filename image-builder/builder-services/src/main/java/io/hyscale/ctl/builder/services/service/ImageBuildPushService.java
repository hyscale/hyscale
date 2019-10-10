package io.hyscale.ctl.builder.services.service;

import io.hyscale.ctl.builder.core.models.BuildContext;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

public interface ImageBuildPushService {

	public void buildAndPush(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException;
}
