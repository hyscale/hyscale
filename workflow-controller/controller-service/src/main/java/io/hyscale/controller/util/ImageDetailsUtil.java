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
package io.hyscale.controller.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class ImageDetailsUtil {
	private static final Logger logger = LoggerFactory.getLogger(ImageDetailsUtil.class);

	public static boolean isImageBuildPushRequired(ServiceSpec serviceSpec) {

		BuildSpec buildSpec = null;
		Dockerfile dockerfile = null;
		try {
			buildSpec = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec),
					BuildSpec.class);
			dockerfile = serviceSpec
					.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile), Dockerfile.class);
		} catch (HyscaleException e) {
			logger.info("Error while fetching buildSpec and registryUrl from serviceSpec ");
		}
		if (buildSpec == null && dockerfile == null) {
			return false;
		} else {
			return true;
		}
	}

}
