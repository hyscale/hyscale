package io.hyscale.ctl.builder.services.service;

import io.hyscale.ctl.builder.core.models.BuildContext;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

/**
 * Interface to build & push from the service spec
 * if buildSpec is defined in the servicespec
 * <p>Implementation Notes</p>
 * Implementations to this interface should be responsible for
 * building the image either from the dockerfile that was defined servicespec or
 * from BuildContext.
 * After a successful build , image has to be tagged with the image directive
 * specified in the service spec and push it to the #BuildContext.imageRegistry
 *
 */

public interface ImageBuildPushService {

	/**
	 *  Builds the image either from the dockerfile that was defined servicespec or
	 *  from BuildContext.
	 *  After a successful build , image has to be tagged with the image directive
	 *  specified in the service spec and push it to the #BuildContext.imageRegistry
	 *
	 * @param serviceSpec servicespec
	 * @param context  parameters that control the image build & image push
	 * @throws HyscaleException
	 */

	public void buildAndPush(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException;
}
