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
package io.hyscale.generator.services.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class ManifestContextTestUtil {
    
    public static ManifestContext getManifestContext(ManifestResource podSpecOwner) {
        ManifestContext context = new ManifestContext();
        context.setAppName("app");
        context.setEnvName("dev");
        if (podSpecOwner != null) {
            context.addGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER, podSpecOwner.getKind());
        }
        return context;
    }

    public static LoadBalancer getLoadBalancerFromSpec(ServiceSpec serviceSpec) throws HyscaleException {
        LoadBalancer loadBalancer = serviceSpec.get(HyscaleSpecFields.loadBalancer, new TypeReference<LoadBalancer>(){});
        return loadBalancer;
    }

}
