package io.hyscale.ctl.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.ctl.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.model.MetaDataContext;
import io.hyscale.ctl.generator.services.predicates.ManifestPredicates;
import io.hyscale.ctl.plugin.framework.handler.ManifestHandler;
import io.hyscale.ctl.plugin.framework.models.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.plugin.framework.util.JsonSnippetConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ManifestPlugin(name = "SelectorLabelsHandler")
public class SelectorLabelsHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(SelectorLabelsHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setAppName(manifestContext.getAppName());
        metaDataContext.setEnvName(manifestContext.getEnvName());
        metaDataContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        List<ManifestSnippet> snippetList = new ArrayList<>();
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec)
                ? ManifestResource.STATEFUL_SET.getKind()
                : ManifestResource.DEPLOYMENT.getKind();
        try {
            snippetList.add(getPodSpecSelectorLabels(metaDataContext, podSpecOwner));
            if (ManifestResource.SERVICE.getPredicate().test(serviceSpec)) {
                logger.debug("Checking  for service ports in spec and adding service seletor labels to the snippet.");
                snippetList.add(getServiceSelectorLabels(metaDataContext, podSpecOwner));
            }
        } catch (JsonProcessingException e) {
            logger.error("Error while serializing pod spec labels snippet ", e);
        }
        return snippetList;
    }

    private ManifestSnippet getServiceSelectorLabels(MetaDataContext metaDataContext, String podSpecOwner)
            throws JsonProcessingException {
        ManifestSnippet selectorSnippet = new ManifestSnippet();
        selectorSnippet.setPath("spec.selector");
        selectorSnippet.setKind(ManifestResource.SERVICE.getKind());
        selectorSnippet.setSnippet(JsonSnippetConvertor.serialize(getSelectorLabels(metaDataContext, podSpecOwner)));
        return selectorSnippet;
    }

    private ManifestSnippet getPodSpecSelectorLabels(MetaDataContext metaDataContext, String podSpecOwner)
            throws JsonProcessingException {
        ManifestSnippet selectorSnippet = new ManifestSnippet();
        selectorSnippet.setPath("spec.selector.matchLabels");
        selectorSnippet.setKind(podSpecOwner);
        selectorSnippet.setSnippet(JsonSnippetConvertor.serialize(getSelectorLabels(metaDataContext, podSpecOwner)));
        return selectorSnippet;
    }

    private Map<String, String> getSelectorLabels(MetaDataContext metaDataContext, String podSpecOwner) {
        ManifestResource manifestResource = ManifestResource.fromString(podSpecOwner);
        if (manifestResource == null) {
            return null;
        }
        return manifestResource.getLabels(metaDataContext);
    }

}
