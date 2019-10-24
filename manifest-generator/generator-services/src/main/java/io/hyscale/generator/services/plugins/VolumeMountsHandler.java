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
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.model.MetaDataContext;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.generator.services.generator.K8sResourceNameGenerator;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.model.service.Volume;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.models.V1VolumeMount;
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
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        metaDataContext.setEnvName(manifestContext.getEnvName());
        metaDataContext.setAppName(manifestContext.getAppName());

        List<ManifestSnippet> snippetList = new ArrayList<>();
        // Build Volume Mounts from dataDir
        try {
            snippetList.add(buildVolumeMountSnippet(serviceSpec, metaDataContext));
        } catch (JsonProcessingException e) {
            logger.error("Error while generating volume mounts manifest for service {}", e);
        }
        return snippetList;
    }

    private List<V1VolumeMount> getVolumeMounts(ServiceSpec serviceSpec, MetaDataContext metaDataContext)
            throws HyscaleException {
        TypeReference<List<Volume>> volumesList = new TypeReference<List<Volume>>() {
        };
        List<Volume> volumes = serviceSpec.get(HyscaleSpecFields.volumes, volumesList);

        List<V1VolumeMount> v1VolumeMounts = new ArrayList<>();
        if (volumes != null && !volumes.isEmpty()) {
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
            V1VolumeMount volumeMount = new V1VolumeMount();
            String configMapName = ManifestResource.CONFIG_MAP.getName(metaDataContext);
            volumeMount.setName(K8sResourceNameGenerator.getResourceVolumeName(configMapName,
                    ManifestResource.CONFIG_MAP.getKind()));
            volumeMount.setMountPath(propsVolumePath);
            volumeMount.setReadOnly(true);
            v1VolumeMounts.add(volumeMount);
        }

        String secretsVolumePath = serviceSpec.get(HyscaleSpecFields.secretsVolumePath, String.class);
        // Add only if secrets are present
        if (ManifestPredicates.haveSecretsVolume().test(serviceSpec)
                && ManifestResource.SECRET.getPredicate().test(serviceSpec)) {
            logger.debug("Preparing volume mount for service spec secrets.");
            V1VolumeMount volumeMount = new V1VolumeMount();
            String secretName = ManifestResource.SECRET.getName(metaDataContext);
            volumeMount.setName(
                    K8sResourceNameGenerator.getResourceVolumeName(secretName, ManifestResource.SECRET.getKind()));
            volumeMount.setMountPath(secretsVolumePath);
            volumeMount.setReadOnly(true);
            v1VolumeMounts.add(volumeMount);
        }
        return v1VolumeMounts.isEmpty() ? null : v1VolumeMounts;
    }

    private ManifestSnippet buildVolumeMountSnippet(ServiceSpec serviceSpec, MetaDataContext metaDataContext) throws JsonProcessingException, HyscaleException {
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();
        TypeReference<List<Volume>> volumesList = new TypeReference<List<Volume>>() {
        };
        ManifestSnippet volumeMountSnippet = new ManifestSnippet();
        volumeMountSnippet.setSnippet(JsonSnippetConvertor.serialize(getVolumeMounts(serviceSpec, metaDataContext)));
        volumeMountSnippet.setKind(podSpecOwner);
        volumeMountSnippet.setPath("spec.template.spec.containers[0].volumeMounts");
        return volumeMountSnippet;
    }
}
