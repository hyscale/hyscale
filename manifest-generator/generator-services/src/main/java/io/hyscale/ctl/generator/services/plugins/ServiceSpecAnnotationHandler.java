package io.hyscale.ctl.generator.services.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.ctl.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.AnnotationKey;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.predicates.ManifestPredicates;
import io.hyscale.ctl.plugin.framework.handler.ManifestHandler;
import io.hyscale.ctl.plugin.framework.models.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.plugin.framework.util.JsonSnippetConvertor;

@Component
@ManifestPlugin(name = "ServiceSpecAnnotationHandler")
public class ServiceSpecAnnotationHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ServiceSpecAnnotationHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec)
                ? ManifestResource.STATEFUL_SET.getKind()
                : ManifestResource.DEPLOYMENT.getKind();
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("metadata.annotations");
        ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
        try {
            Map<String, String> annotations = new HashMap<>();
            annotations.put(AnnotationKey.HYSCALE_SERVICE_SPEC.getAnnotation(),mapper.writeValueAsString(serviceSpec.toString()));
            manifestSnippet.setSnippet(JsonSnippetConvertor.serialize(annotations));
            manifestSnippetList.add(manifestSnippet);
            return manifestSnippetList;
        } catch (JsonProcessingException e) {
            logger.error("Error while processing service spec annotations");
            return null;
        }
    }
}
