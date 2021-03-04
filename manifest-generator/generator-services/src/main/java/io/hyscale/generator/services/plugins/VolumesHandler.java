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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.generator.services.generator.K8sResourceNameGenerator;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.openapi.models.V1ConfigMapVolumeSource;
import io.kubernetes.client.openapi.models.V1SecretVolumeSource;
import io.kubernetes.client.openapi.models.V1Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "VolumesHandler")
public class VolumesHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(VolumesHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext)
            throws HyscaleException {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        serviceMetadata.setEnvName(manifestContext.getEnvName());
        serviceMetadata.setAppName(manifestContext.getAppName());
        String podSpecOwner = ((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        try {
            snippetList.add(buildVolumeSnippet(serviceSpec, serviceMetadata, podSpecOwner));
        } catch (JsonProcessingException e) {
            logger.error("Error while generating volume mounts manifest for service {}", serviceMetadata.getServiceName(), e);
        }
        return snippetList;
    }

    private ManifestSnippet buildVolumeSnippet(ServiceSpec serviceSpec, ServiceMetadata serviceMetadata,
                                               String podSpecOwner) throws JsonProcessingException, HyscaleException {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.volumes");
        manifestSnippet.setSnippet(JsonSnippetConvertor.serialize(getVolumes(serviceSpec, serviceMetadata)));
        return manifestSnippet;
    }

    private List<V1Volume> getVolumes(ServiceSpec serviceSpec, ServiceMetadata serviceMetadata)
            throws HyscaleException {
        List<V1Volume> volumes = new ArrayList<>();
        if (ManifestPredicates.haveConfigmapVolume().test(serviceSpec)
                && ManifestPredicates.getPropsPredicate().test(serviceSpec)) {
            V1Volume v1Volume = new V1Volume();
            String configMapName = ManifestResource.CONFIG_MAP.getName(serviceMetadata);
            v1Volume.setName(K8sResourceNameGenerator.getResourceVolumeName(configMapName,
                    ManifestResource.CONFIG_MAP.getKind()));
            V1ConfigMapVolumeSource v1ConfigMapVolumeSource = new V1ConfigMapVolumeSource();
            v1ConfigMapVolumeSource.setName(configMapName);
            v1Volume.setConfigMap(v1ConfigMapVolumeSource);
            volumes.add(v1Volume);
            logger.debug("Preparing  volumes for props.");
        }
        if (ManifestPredicates.haveSecretsVolume().test(serviceSpec)
                && ManifestPredicates.getSecretsEnvPredicate().test(serviceSpec)) {
            V1Volume v1Volume = new V1Volume();
            String secretName = ManifestResource.SECRET.getName(serviceMetadata);
            v1Volume.setName(
                    K8sResourceNameGenerator.getResourceVolumeName(secretName, ManifestResource.SECRET.getKind()));
            V1SecretVolumeSource v1SecretVolumeSource = new V1SecretVolumeSource();
            v1SecretVolumeSource.secretName(secretName);
            v1Volume.setSecret(v1SecretVolumeSource);
            volumes.add(v1Volume);
            logger.debug("Preparing volumes for secrets.");
        }
        return !volumes.isEmpty() ? volumes : null;
    }
}
