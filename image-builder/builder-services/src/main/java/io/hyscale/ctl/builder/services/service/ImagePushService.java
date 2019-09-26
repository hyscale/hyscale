package io.hyscale.ctl.builder.services.service;

import io.hyscale.ctl.builder.core.models.BuildContext;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

public interface ImagePushService {

	/**
	 * Check docker exists, Login, tag, push if required
	 * 
	 * @param serviceSpec
	 * @param buildContext
	 * @throws HyscaleException
	 */
	public void pushImage(ServiceSpec serviceSpec, BuildContext buildContext) throws HyscaleException;
}