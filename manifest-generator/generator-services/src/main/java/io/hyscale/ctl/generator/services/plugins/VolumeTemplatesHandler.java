package io.hyscale.ctl.generator.services.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.ctl.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.constants.K8SRuntimeConstants;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.commons.models.VolumeAccessMode;
import io.hyscale.ctl.generator.services.constants.ManifestGenConstants;
import io.hyscale.ctl.generator.services.exception.ManifestErrorCodes;
import io.hyscale.ctl.generator.services.model.ManifestGeneratorActivity;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.model.MetaDataContext;
import io.hyscale.ctl.plugin.framework.handler.ManifestHandler;
import io.hyscale.ctl.plugin.framework.models.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.servicespec.commons.model.service.Volume;
import io.hyscale.ctl.plugin.framework.util.GsonSnippetConvertor;
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
        if (!validateVolumes(volumes, manifestContext)) {
            logger.debug("Validation for volume templates failed.");
            return null;
        }

        String serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setServiceName(serviceName);
        metaDataContext.setEnvName(manifestContext.getEnvName());
        metaDataContext.setAppName(manifestContext.getAppName());

        List<ManifestSnippet> snippetList = new ArrayList<>();

        try {
            // Creating a manifest snippet for volumeClaimTemplates
            snippetList.add(buildVolumeClaimSnippet(volumes, metaDataContext));
            manifestContext.addGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER,
                    ManifestResource.STATEFUL_SET.getKind());

        } catch (JsonProcessingException e) {
            logger.error("Error while serializing volumes snippet ", e);
        }
        return snippetList.isEmpty() ? null : snippetList;
    }

    private ManifestSnippet buildVolumeClaimSnippet(List<Volume> volumes, MetaDataContext metaDataContext)
            throws JsonProcessingException, HyscaleException {
        ManifestSnippet snippet = new ManifestSnippet();
        snippet.setSnippet(GsonSnippetConvertor.serialize(getVolumeClaims(volumes, metaDataContext)));
        snippet.setKind(ManifestResource.STATEFUL_SET.getKind());
        snippet.setPath("spec.volumeClaimTemplates");
        return snippet;
    }

    private List<V1PersistentVolumeClaim> getVolumeClaims(List<Volume> volumes, MetaDataContext metaDataContext)
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
            v1ObjectMeta.setLabels(ManifestResource.STATEFUL_SET.getLabels(metaDataContext));

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
            manifestContext.addGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER,
                    ManifestResource.DEPLOYMENT.getKind());
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
}
