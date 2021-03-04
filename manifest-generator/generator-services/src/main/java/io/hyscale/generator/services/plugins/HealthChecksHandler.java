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
package io.hyscale.generator.services.plugins;

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.builder.DefaultHealthChecksBuilder;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ManifestPlugin(name = "HealthChecksHandler")
public class HealthChecksHandler implements ManifestHandler {

    @Autowired
    private DefaultHealthChecksBuilder defaultHealthChecksBuilder;

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException {
        List<Port> portsList = serviceSpec.get(HyscaleSpecFields.ports, new TypeReference<>() {
        });
        String podSpecOwner = ((String) context.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
        return defaultHealthChecksBuilder.generateHealthCheckSnippets(portsList, podSpecOwner);
    }

}
