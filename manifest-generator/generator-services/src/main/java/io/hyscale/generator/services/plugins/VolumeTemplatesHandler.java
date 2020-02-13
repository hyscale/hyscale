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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.model.ManifestGeneratorActivity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.models.VolumeAccessMode;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.model.AppMetaData;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.model.service.Volume;
import io.hyscale.plugin.framework.util.GsonSnippetConvertor;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.QuantityFormatException;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1PersistentVolumeClaimSpec;
import io.kubernetes.client.models.V1ResourceRequirements;

@Component
@ManifestPlugin(name = "VolumeTemplatesHandler")
public class VolumeTemplatesHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(VolumeTemplatesHandler.class);

    private static final String PVC_DEFAULT_STORAGE_UNIT = "Gi";

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext)
            throws HyscaleException {

        TypeReference<List<Volume>> volumesList = new TypeReference<List<Volume>>() {
        };
        List<Volume> volumes = serviceSpec.get(HyscaleSpecFields.volumes, volumesList);
        String podSpecOwner = (String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER);
        if (!validateVolumes(volumes, manifestContext) || !(podSpecOwner.equals(ManifestResource.DEPLOYMENT.getKind()) || podSpecOwner.equals(ManifestResource.STATEFUL_SET.getKind()))) {
            logger.debug("Validation for volume templates failed.");
            return null;
        }

        String serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
        AppMetaData appMetaData = new AppMetaData();
        appMetaData.setServiceName(serviceName);
        appMetaData.setEnvName(manifestContext.getEnvName());
        appMetaData.setAppName(manifestContext.getAppName());

        List<ManifestSnippet> snippetList = new ArrayList<>();


        try {
            // Creating a manifest snippet for volumeClaimTemplates
            snippetList.add(buildVolumeClaimSnippet(volumes, appMetaData));
            snippetList.add(getServiceNameSnippet(appMetaData.getServiceName()));
        } catch (JsonProcessingException e) {
            logger.error("Error while serializing volumes snippet ", e);
        }
        return snippetList.isEmpty() ? null : snippetList;
    }

    private ManifestSnippet buildVolumeClaimSnippet(List<Volume> volumes, AppMetaData appMetaData)
            throws JsonProcessingException, HyscaleException {
        ManifestSnippet snippet = new ManifestSnippet();
        snippet.setSnippet(GsonSnippetConvertor.serialize(getVolumeClaims(volumes, appMetaData)));
        snippet.setKind(ManifestResource.STATEFUL_SET.getKind());
        snippet.setPath("spec.volumeClaimTemplates");
        return snippet;
    }

    private List<V1PersistentVolumeClaim> getVolumeClaims(List<Volume> volumes, AppMetaData appMetaData)
            throws HyscaleException {
        List<V1PersistentVolumeClaim> volumeClaims = new LinkedList<>();
        for (Volume volume : volumes) {
            String size = volume.getSize();
            if (StringUtils.isBlank(size)) {
                logger.debug("Volume size not found, assigning default value {}.",K8SRuntimeConstants.DEFAULT_VOLUME_SIZE);
                size = K8SRuntimeConstants.DEFAULT_VOLUME_SIZE;
            } else {
                parseSize(size);
            }
            V1PersistentVolumeClaim v1PersistentVolumeClaim = new V1PersistentVolumeClaim();
            V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
            v1ObjectMeta.setName(volume.getName());
            v1ObjectMeta.setLabels(ManifestResource.STATEFUL_SET.getLabels(appMetaData));

            V1PersistentVolumeClaimSpec claimSpec = new V1PersistentVolumeClaimSpec();
            claimSpec.setAccessModes(Arrays.asList(VolumeAccessMode.READ_WRITE_ONCE.getAccessMode()));
            if(volume.getStorageClass()!=null) {
                claimSpec.setStorageClassName(volume.getStorageClass());
            }
            Map<String, Quantity> requests = new HashMap<>();
            requests.put("storage", Quantity.fromString(size));

            V1ResourceRequirements resourceRequirements = new V1ResourceRequirements();
            resourceRequirements.setRequests(requests);
            claimSpec.setResources(resourceRequirements);

            v1PersistentVolumeClaim.setMetadata(v1ObjectMeta);
            v1PersistentVolumeClaim.setSpec(claimSpec);
            volumeClaims.add(v1PersistentVolumeClaim);

        }
        return volumeClaims.isEmpty() ? null : volumeClaims;
    }

    private void parseSize(String size) throws HyscaleException {
        try {
            Quantity.fromString(size);
        } catch (QuantityFormatException e) {
            WorkflowLogger.persist(ManifestGeneratorActivity.INVALID_SIZE_FORMAT, size);
            throw new HyscaleException(ManifestErrorCodes.INVALID_SIZE_FORMAT, e.getMessage());
        }
    }

    private boolean validateVolumes(List<Volume> volumes, ManifestContext manifestContext) throws HyscaleException {
        if (volumes == null || volumes.isEmpty()) {
            logger.debug("No volumes found.");
            return false;
        }

        /*for (Volume volume : volumes) {
            if (StringUtils.isBlank(volume.getStorageClass())) {
                logger.debug("Storage class for volume {} found to be empty.",volume);
                WorkflowLogger.persist(ManifestGeneratorActivity.MISSING_FIELD, HyscaleSpecFields.storageClass);
                HyscaleException he = new HyscaleException(ManifestErrorCodes.MISSING_STORAGE_CLASS_FOR_VOLUMES, volume.getName());
                throw he;
            }
        }*/
        return true;
    }

    private ManifestSnippet getServiceNameSnippet(String serviceName){
        ManifestSnippet snippet =new ManifestSnippet();
        snippet.setSnippet(serviceName);
        snippet.setKind(ManifestResource.STATEFUL_SET.getKind());
        snippet.setPath("spec.serviceName");
        return snippet;
    }
}
