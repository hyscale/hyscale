package io.hyscale.ctl.generator.services.plugins;

import io.hyscale.ctl.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.predicates.ManifestPredicates;
import io.hyscale.ctl.plugin.ManifestHandler;
import io.hyscale.ctl.plugin.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "ReplicasHandler")
public class ReplicasHandler implements ManifestHandler {
    private static final Logger logger = LoggerFactory.getLogger(ReplicasHandler.class);
    private static final String DEFAULT_UPDATE_STRATEGY = "RollingUpdate";

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        Integer replicas = serviceSpec.get(HyscaleSpecFields.replicas, Integer.class);
        // In user does not specify replicas field in hspec, by default we consider a single replica
        if (replicas == null || replicas == 0 ) {
            logger.debug("Cannot find replicas,setting default value to 1.");
            replicas = 1;
        }

        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();

        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        ManifestSnippet replicaSnippet = new ManifestSnippet();
        replicaSnippet.setSnippet(String.valueOf(replicas));
        replicaSnippet.setPath("spec.replicas");
        replicaSnippet.setKind(podSpecOwner);

        manifestSnippetList.add(replicaSnippet);
        return manifestSnippetList;
    }
}
