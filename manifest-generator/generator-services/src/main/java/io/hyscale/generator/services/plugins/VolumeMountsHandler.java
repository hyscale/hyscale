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
import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.utils.VolumeMountsUtil;
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
import io.hyscale.servicespec.commons.model.service.Volume;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.openapi.models.V1VolumeMount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "VolumeMountsHandler")
public class VolumeMountsHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(VolumeMountsHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        serviceMetadata.setEnvName(manifestContext.getEnvName());
        serviceMetadata.setAppName(manifestContext.getAppName());
        String podSpecOwner = ((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        // Build Volume Mounts from dataDir
        try {
            snippetList.add(buildVolumeMountSnippet(serviceSpec, serviceMetadata, podSpecOwner));
        } catch (JsonProcessingException e) {
            logger.error("Error while generating volume mounts manifest for service {}", serviceMetadata.getServiceName(), e);
        }
        return snippetList;
    }

    private List<V1VolumeMount> getVolumeMounts(ServiceSpec serviceSpec, ServiceMetadata serviceMetadata, String podSpecOwner)
            throws HyscaleException {
        TypeReference<List<Volume>> volumesList = new TypeReference<List<Volume>>() {
        };

        List<Volume> volumes = serviceSpec.get(HyscaleSpecFields.volumes, volumesList);

        List<V1VolumeMount> v1VolumeMounts = new ArrayList<>();
        if ((volumes != null && !volumes.isEmpty()) && (podSpecOwner.equals(ManifestResource.DEPLOYMENT.getKind()) || podSpecOwner.equals(ManifestResource.STATEFUL_SET.getKind()))) {
            logger.debug("Preparing volume mount for service spec volumes.");
            volumes.stream().forEach(volume -> {
                V1VolumeMount volumeMount = new V1VolumeMount();
                volumeMount.setName(volume.getName());
                volumeMount.setMountPath(volume.getPath());
                volumeMount.setReadOnly(volume.isReadOnly());
                v1VolumeMounts.add(volumeMount);
            });
        }

        String propsVolumePath = serviceSpec.get(HyscaleSpecFields.propsVolumePath, String.class);
        //Add only if props are present
        if (ManifestPredicates.haveConfigmapVolume().test(serviceSpec)
                && ManifestResource.CONFIG_MAP.getPredicate().test(serviceSpec)) {
            logger.debug("Preparing volume mount for service spec props.");
            String configMapName = ManifestResource.CONFIG_MAP.getName(serviceMetadata);
            v1VolumeMounts.add(VolumeMountsUtil.buildForProps(propsVolumePath,K8sResourceNameGenerator.getResourceVolumeName(configMapName,
                    ManifestResource.CONFIG_MAP.getKind())));
        }

        String secretsVolumePath = serviceSpec.get(HyscaleSpecFields.secretsVolumePath, String.class);
        // Add only if secrets are present
        if (ManifestPredicates.haveSecretsVolume().test(serviceSpec)
                && ManifestResource.SECRET.getPredicate().test(serviceSpec)) {
            logger.debug("Preparing volume mount for service spec secrets.");
            String secretName = ManifestResource.SECRET.getName(serviceMetadata);
            v1VolumeMounts.add(VolumeMountsUtil.buildForSecrets(secretsVolumePath,K8sResourceNameGenerator.getResourceVolumeName(secretName,
                    ManifestResource.SECRET.getKind())));
        }
        return v1VolumeMounts.isEmpty() ? null : v1VolumeMounts;
    }

    private ManifestSnippet buildVolumeMountSnippet(ServiceSpec serviceSpec, ServiceMetadata serviceMetadata, String podSpecOwner) throws JsonProcessingException, HyscaleException {
        ManifestSnippet volumeMountSnippet = new ManifestSnippet();
        volumeMountSnippet.setSnippet(JsonSnippetConvertor.serialize(getVolumeMounts(serviceSpec, serviceMetadata, podSpecOwner)));
        volumeMountSnippet.setKind(podSpecOwner);
        volumeMountSnippet.setPath("spec.template.spec.containers[0].volumeMounts");
        return volumeMountSnippet;
    }
}
