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
package io.hyscale.builder.cleanup.services;

import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 *  This class preserves the last 'n' service images built by hyscale, where
 * (n = {@link io.hyscale.builder.services.config.ImageBuilderConfig#getNoOfPreservedImages() }).
 * Hyscale adds a label to the image as imageowner = hyscale. 
 * This clean up is applicable for all
 * those images which are tagged with the label imageowner = hyscale
 *
 */
public abstract class PreserveLastNUsed implements ImageCleanupProcessor {

    public abstract void clean(ServiceSpec serviceSpec);

}
