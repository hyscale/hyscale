package io.hyscale.ctl.generator.services.plugins;

import io.hyscale.ctl.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.constants.ManifestGenConstants;
import io.hyscale.ctl.generator.services.predicates.ManifestPredicates;
import io.hyscale.ctl.plugin.ManifestHandler;
import io.hyscale.ctl.plugin.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.servicespec.commons.util.ImageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "ImageHandler")
public class ImageHandler implements ManifestHandler {

    private static final String DELIMITER = "/";
    private static final String DEFAULT_IMAGE_PULL_POLICY = "Always";
    private static final Logger logger = LoggerFactory.getLogger(ImageHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext)
            throws HyscaleException {
        String image = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.name), String.class);
        if (StringUtils.isBlank(image)) {
            logger.debug("Found empty image in the service spec , cannot process imagehandler ");
            return null;
        }
        List<ManifestSnippet> snippetList = new ArrayList<>();
        snippetList.add(getImageSnippet(serviceSpec, manifestContext));
        snippetList.add(getImagePullPolicy(serviceSpec));
        return snippetList;
    }

    private ManifestSnippet getImagePullPolicy(ServiceSpec serviceSpec) {
        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setSnippet(DEFAULT_IMAGE_PULL_POLICY);
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec)
                ? ManifestResource.STATEFUL_SET.getKind()
                : ManifestResource.DEPLOYMENT.getKind();
        manifestSnippet.setKind(podSpecOwner);
        manifestSnippet.setPath("spec.template.spec.containers[0].imagePullPolicy");
        return manifestSnippet;
    }

    private ManifestSnippet getImageSnippet(ServiceSpec serviceSpec, ManifestContext manifestContext)
            throws HyscaleException {
        String imageShaId = (String) manifestContext.getGenerationAttribute(ManifestGenConstants.IMAGE_SHA_SUM);
        String image = null;
        if (StringUtils.isNotBlank(imageShaId)) {
            logger.debug("Image built from platform ,preparing image with its digest.");
            image = imageShaId;
        } else {
            image = ImageUtil.getImage(serviceSpec);
            logger.debug("Preparing image directly from given tag.");
        }
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec)
                ? ManifestResource.STATEFUL_SET.getKind()
                : ManifestResource.DEPLOYMENT.getKind();
        ManifestSnippet imageSnippet = new ManifestSnippet();
        imageSnippet.setSnippet(image);
        imageSnippet.setPath("spec.template.spec.containers[0].image");
        imageSnippet.setKind(podSpecOwner);
        return imageSnippet;
    }

}
