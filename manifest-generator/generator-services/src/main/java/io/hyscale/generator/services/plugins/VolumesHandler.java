package io.hyscale.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.models.V1ConfigMapVolumeSource;
import io.kubernetes.client.models.V1SecretVolumeSource;
import io.kubernetes.client.models.V1Volume;
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
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        metaDataContext.setEnvName(manifestContext.getEnvName());
        metaDataContext.setAppName(manifestContext.getAppName());

        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec)
                ? ManifestResource.STATEFUL_SET.getKind()
                : ManifestResource.DEPLOYMENT.getKind();
        List<ManifestSnippet> snippetList = new ArrayList<>();
        try {
            snippetList.add(buildVolumeSnippet(serviceSpec, metaDataContext, podSpecOwner));
        } catch (JsonProcessingException e) {
            logger.error("Error while generating volume mounts manifest for service {}", e);
        }
        return snippetList;
    }

    private ManifestSnippet buildVolumeSnippet(ServiceSpec serviceSpec, MetaDataContext metaDataContext,
                                               String podSpecOwner) throws JsonProcessingException, HyscaleException {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.volumes");
        manifestSnippet.setSnippet(JsonSnippetConvertor.serialize(getVolumes(serviceSpec, metaDataContext)));
        return manifestSnippet;
    }

    private List<V1Volume> getVolumes(ServiceSpec serviceSpec, MetaDataContext metaDataContext)
            throws HyscaleException {
        List<V1Volume> volumes = new ArrayList<>();
        if (ManifestPredicates.haveConfigmapVolume().test(serviceSpec)
                && ManifestPredicates.getPropsPredicate().test(serviceSpec)) {
            V1Volume v1Volume = new V1Volume();
            String configMapName = ManifestResource.CONFIG_MAP.getName(metaDataContext);
            v1Volume.setName(K8sResourceNameGenerator.getResourceVolumeName(configMapName,
                    ManifestResource.CONFIG_MAP.getKind()));
            V1ConfigMapVolumeSource v1ConfigMapVolumeSource = new V1ConfigMapVolumeSource();
            v1ConfigMapVolumeSource.setName(configMapName);
            v1Volume.setConfigMap(v1ConfigMapVolumeSource);
            volumes.add(v1Volume);
            logger.debug("preparing  volumes for props.");
        }

        if (ManifestPredicates.haveSecretsVolume().test(serviceSpec)
                && ManifestPredicates.getSecretsEnvPredicate().test(serviceSpec)) {
            V1Volume v1Volume = new V1Volume();
            String secretName = ManifestResource.SECRET.getName(metaDataContext);
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
