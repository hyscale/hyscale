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
package io.hyscale.generator.services.generator;

import java.util.List;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Interface to generate kubernetes manifests from the service spec
 * <p>Implementation Notes</p>
 * Implementations to this interface should generate a kubernetes manifest {@link Manifest}
 *
 * @see <a href="https://github.com/hyscale/hyscale/blob/master/docs/contributor-guide.md#manifest-generator-module">Reference</a>
 * for more detailed reference of manifest generation
 *
 */

public interface ManifestGenerator {

    /**
     * Generates the manifest @see {@link Manifest} from the given service spec
     *
     * @param serviceSpec servicespec
     * @param context     consist of parameters which control the manifest generation
     * @throws HyscaleException
     * @return Manifests List
     */
    public List<Manifest> generate(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException;
}
