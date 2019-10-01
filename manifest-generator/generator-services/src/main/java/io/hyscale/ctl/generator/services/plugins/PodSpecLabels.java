package io.hyscale.ctl.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.ctl.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.model.MetaDataContext;
import io.hyscale.ctl.generator.services.predicates.ManifestPredicates;
import io.hyscale.ctl.plugin.ManifestHandler;
import io.hyscale.ctl.plugin.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.util.JsonSnippetConvertor;
import io.kubernetes.client.models.V1ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "PodSpecLabels")
public class PodSpecLabels implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(PodSpecLabels.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setAppName(manifestContext.getAppName());
        metaDataContext.setEnvName(manifestContext.getEnvName());
        metaDataContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();
        try {
            ManifestSnippet metaDataSnippet = new ManifestSnippet();
            metaDataSnippet.setPath("spec.template.metadata");
            metaDataSnippet.setKind(podSpecOwner);
            metaDataSnippet.setSnippet(JsonSnippetConvertor.serialize(getTemplateMetaData(metaDataContext, podSpecOwner)));
            snippetList.add(metaDataSnippet);

        } catch (JsonProcessingException e) {
            logger.error("Error while serializing pod spec labels snippet ", e);
        }
        return snippetList;
    }

    private V1ObjectMeta getTemplateMetaData(MetaDataContext metaDataContext, String podSpecOwner) {
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        ManifestResource manifestResource = ManifestResource.fromString(podSpecOwner);
        if (manifestResource == null) {
            return null;
        }
        //TODO Add release-version ??
        v1ObjectMeta.setLabels(manifestResource.getLabels(metaDataContext));
        return v1ObjectMeta;
    }
}
