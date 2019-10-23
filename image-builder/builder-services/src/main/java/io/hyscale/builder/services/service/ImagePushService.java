package io.hyscale.builder.services.service;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

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