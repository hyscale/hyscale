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
package io.hyscale.builder.services.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.cleanup.services.ImageCleanupProcessor;
import io.hyscale.builder.cleanup.services.impl.DeleteAfterBuild;
import io.hyscale.builder.cleanup.services.impl.DeleteAll;
import io.hyscale.builder.cleanup.services.impl.PreserveAll;
import io.hyscale.builder.cleanup.services.impl.PreserveLastNUsed;
import io.hyscale.builder.core.models.ImageCleanUpPolicy;

@Component
public class ImageCleanupProcessorFactory {

	@Autowired
	private DeleteAll deleteAll;
	@Autowired
	private DeleteAfterBuild deleteAfterBuild;
	@Autowired
	private PreserveLastNUsed preserve_Last_N_USED;
	@Autowired
	private PreserveAll preserveAll;

	public ImageCleanupProcessor getImageCleanupProcessor(String imageCleanUpPolicy) {
		return getImageCleanupProcessor(ImageCleanUpPolicy.valueOf(imageCleanUpPolicy));
	}

	public ImageCleanupProcessor getImageCleanupProcessor(ImageCleanUpPolicy policy) {

		switch (policy) {

		case DELETE_AFTER_BUILD:
			return deleteAfterBuild;

		case PRESERVE_N_RECENTLY_USED:
			return preserve_Last_N_USED;

		case PRESERVE_ALL:
			return preserveAll;

		case DELETE_ALL:
			return deleteAll;

		default:
			return deleteAfterBuild;
		}
	}
}
