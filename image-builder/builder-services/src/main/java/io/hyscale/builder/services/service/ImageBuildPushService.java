/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.builder.services.service;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Interface to build and push from the service spec
 * If buildSpec is defined in the service spec
 * <p>Implementation Notes</p>
 * Implementations of this interface should be responsible for
 * building the image either from the dockerfile that was defined in service spec or
 * from BuildContext.
 * After a successful build , image has to be tagged with the image directive
 * specified in the service spec and push it to the #BuildContext.imageRegistry
 *
 */

public interface ImageBuildPushService {

	/**
	 *  Builds the image either from the dockerfile that was defined in service spec or
	 *  from BuildContext.
	 *  After a successful build , image has to be tagged with the image directive
	 *  specified in the service spec and push it to the #BuildContext.imageRegistry
	 *
	 * @param serviceSpec servicespec
	 * @param context  parameters that control the image build and image push
	 * @throws HyscaleException
	 */

	public void buildAndPush(ServiceSpec serviceSpec, BuildContext context) throws HyscaleException;
}
