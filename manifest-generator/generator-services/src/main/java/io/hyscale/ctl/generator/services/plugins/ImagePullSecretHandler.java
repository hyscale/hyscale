package io.hyscale.ctl.generator.services.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.hyscale.ctl.commons.models.DockerConfig;
import io.hyscale.ctl.commons.models.ResourceLabelKey;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.model.MetaDataContext;
import io.hyscale.ctl.generator.services.model.ResourceName;
import io.hyscale.ctl.generator.services.predicates.ManifestPredicates;
import io.hyscale.ctl.generator.services.generator.MetadatManifestSnippetGenerator;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.ctl.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.Auth;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.commons.models.ImageRegistry;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import io.hyscale.ctl.commons.utils.NormalizationUtil;
import io.hyscale.ctl.plugin.framework.handler.ManifestHandler;
import io.hyscale.ctl.plugin.framework.models.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.models.V1ObjectMeta;

@Component
@ManifestPlugin(name = "ImagePullSecretHandler")
public class ImagePullSecretHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ImagePullSecretHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        ImageRegistry imageRegistry = manifestContext.getImageRegistry();
        if (imageRegistry == null) {
            logger.debug("ImageRegistry is null,no image pull secret manifest snippet.");
            return null;
        }

        String name = imageRegistry.getName() == null ? imageRegistry.getUrl() : imageRegistry.getName();
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setAppName(manifestContext.getAppName());
        metaDataContext.setEnvName(manifestContext.getEnvName());
        metaDataContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        logger.debug("Generated image pull secret metadata.");
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        try {
            // Override the name because image-pull-secret has either the registry-name or registry-url as the name of the manifest
            ManifestSnippet apiVersionSnippet = MetadatManifestSnippetGenerator.getApiVersion(ManifestResource.SECRET, metaDataContext);
            //Api Version snippet
            manifestSnippetList.add(apiVersionSnippet);

            // Get the secret kind of image pull secret
            manifestSnippetList.add(MetadatManifestSnippetGenerator.getKind(ManifestResource.SECRET));
            // Get the labels of secret as image pull secret also have the same set of labels
            manifestSnippetList.add(getMetaDataSnippet(metaDataContext, name));
            logger.debug("Added labels of secret as image pull secret also have the same set of labels.");
            // Get the data of image pull secret
            manifestSnippetList.add(getDataSnippet(imageRegistry));
            logger.debug("Added the data of image pull secret.");
            // Get the secret type snippet as kubernetes.io/dockerconfigjson
            manifestSnippetList.add(getSecretTypeSnippet());
            //Add Name to each snippet execpt podSpec ImagePullSecretName
            manifestSnippetList.stream().forEach(each -> {
                each.setName(name);
            });
            // Adding the secret to pod
            logger.debug("Prepared image pull secret for registry.");
            String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ? ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();
            manifestSnippetList.add(getImagePullSecretName(name, podSpecOwner));

        } catch (JsonProcessingException e) {
            logger.error("Error while generating image pull secret manifest {}", e);
        }
        return manifestSnippetList;
    }

    private ManifestSnippet getImagePullSecretName(String name, String podSpecOwner) throws JsonProcessingException {
        List<ResourceName> resourceNameList = new ArrayList<>();
        ResourceName resourceName = new ResourceName();
        resourceName.setName(NormalizationUtil.normalize(name));
        resourceNameList.add(resourceName);
        ManifestSnippet imgPullSecretNamesnippet = new ManifestSnippet();
        imgPullSecretNamesnippet.setKind(podSpecOwner);
        imgPullSecretNamesnippet.setPath("spec.template.spec.imagePullSecrets");
        imgPullSecretNamesnippet.setSnippet(JsonSnippetConvertor.serialize(resourceNameList));
        return imgPullSecretNamesnippet;
    }

    private ManifestSnippet getSecretTypeSnippet() {
        ManifestSnippet secretTypeSnippet = new ManifestSnippet();
        secretTypeSnippet.setKind(ManifestResource.SECRET.getKind());
        secretTypeSnippet.setPath("type");
        secretTypeSnippet.setSnippet("kubernetes.io/dockerconfigjson");
        return secretTypeSnippet;
    }

    private ManifestSnippet getMetaDataSnippet(MetaDataContext metaDataContext, String name)
            throws JsonProcessingException {
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
        v1ObjectMeta.setLabels(ManifestResource.SECRET.getLabels(metaDataContext));
        v1ObjectMeta.setName(NormalizationUtil.normalize(name));
        // to make this secret independent of service
        v1ObjectMeta.getLabels().remove(ResourceLabelKey.SERVICE_NAME.getLabel());

        ManifestSnippet snippet = new ManifestSnippet();
        snippet.setName(name);
        snippet.setSnippet(JsonSnippetConvertor.serialize(v1ObjectMeta));
        snippet.setPath("metadata");
        snippet.setKind(ManifestResource.SECRET.getKind());
        return snippet;
    }

    private ManifestSnippet getDataSnippet(ImageRegistry imageRegistry) throws JsonProcessingException {
        ManifestSnippet snippet = new ManifestSnippet();
        snippet.setName(imageRegistry.getName());
        snippet.setSnippet(JsonSnippetConvertor.serialize(getDockerConfig(imageRegistry)));
        snippet.setPath("data");
        snippet.setKind(ManifestResource.SECRET.getKind());
        return snippet;
    }

    private Map<String, String> getDockerConfig(ImageRegistry imageRegistry) throws JsonProcessingException {
        Map<String, String> dockerAuthConfigMap = new HashMap<>();
        ObjectMapper objectMapper = ObjectMapperFactory.jsonMapper();
        DockerConfig dockerAuthConfig = new DockerConfig();
        Auth auth = new Auth();

        String pair = imageRegistry.getUserName() + ":" + imageRegistry.getPassword();
        String encodedBytes = Base64.encodeBase64String(pair.getBytes());

        auth.setAuth(encodedBytes);
        Map<String, Auth> auths = new HashMap<>();
        auths.put(imageRegistry.getUrl(), auth);
        dockerAuthConfig.setAuths(auths);

        String dockerConfigJson = objectMapper.writeValueAsString(dockerAuthConfig);
        String encodedValue = Base64.encodeBase64String(dockerConfigJson.getBytes());
        dockerAuthConfigMap.put(".dockerconfigjson", encodedValue);
        return dockerAuthConfigMap;
    }
}
