package io.hyscale.generator.services.plugins;

import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "ContainerNameHandler")
public class ContainerNameHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ContainerNameHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ? ManifestResource.STATEFUL_SET.getKind() :
                ManifestResource.DEPLOYMENT.getKind();

        ManifestSnippet containerNameSnippet = new ManifestSnippet();

        containerNameSnippet.setKind(podSpecOwner);
        containerNameSnippet.setPath("spec.template.spec.containers[0].name");
        containerNameSnippet.setSnippet(serviceSpec.get(HyscaleSpecFields.name, String.class));

        List<ManifestSnippet> snippetList = new ArrayList<>();
        snippetList.add(containerNameSnippet);

        return snippetList;

    }
}
