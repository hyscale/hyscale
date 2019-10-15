package io.hyscale.ctl.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.ctl.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.DecoratedArrayList;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.model.MetaDataContext;
import io.hyscale.ctl.generator.services.predicates.ManifestPredicates;
import io.hyscale.ctl.generator.services.provider.PropsProvider;
import io.hyscale.ctl.generator.services.provider.SecretsProvider;
import io.hyscale.ctl.plugin.framework.handler.ManifestHandler;
import io.hyscale.ctl.plugin.framework.models.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.Props;
import io.hyscale.ctl.servicespec.commons.model.service.Secrets;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.models.V1ConfigMapKeySelector;
import io.kubernetes.client.models.V1EnvVar;
import io.kubernetes.client.models.V1EnvVarSource;
import io.kubernetes.client.models.V1SecretKeySelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@ManifestPlugin(name = "PodSpecEnvHandler")
public class PodSpecEnvHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(PodSpecEnvHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        List<ManifestSnippet> snippetList = new ArrayList<>();
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setAppName(manifestContext.getAppName());
        metaDataContext.setEnvName(manifestContext.getEnvName());
        metaDataContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();

        List<V1EnvVar> envVarList = new DecoratedArrayList<V1EnvVar>();
        try {
            // Preparing Pod Spec env's from props
            Props props = PropsProvider.getProps(serviceSpec);
            if (ManifestPredicates.getPropsPredicate().test(serviceSpec)) {
                logger.debug("Preparing Pod Spec env's from props.");
                envVarList.addAll(getPodSpecEnv(props, metaDataContext));
            }

            // Preparing Pod Spec secrets from props
            Secrets secrets = SecretsProvider.getSecrets(serviceSpec);
            if (ManifestPredicates.getSecretsEnvPredicate().test(serviceSpec)) {
                logger.debug("Preparing Pod Spec env's from secrets.");
                envVarList.addAll(getSecretsSnippet(getSecretKeys(secrets), metaDataContext));
            }
            if (envVarList.isEmpty()) {
                return null;
            }

            ManifestSnippet propsEnvSnippet = new ManifestSnippet();
            propsEnvSnippet.setPath("spec.template.spec.containers[0].env");
            propsEnvSnippet.setKind(podSpecOwner);
            propsEnvSnippet.setSnippet(JsonSnippetConvertor.serialize(envVarList));
            snippetList.add(propsEnvSnippet);

        } catch (JsonProcessingException e) {
            logger.error("Error while generating env for pod spec", e);
        }
        return snippetList;
    }

    private List<V1EnvVar> getSecretsSnippet(Set<String> secretKeys, MetaDataContext metaDataContext) {
        if (secretKeys == null || secretKeys.isEmpty()) {
            return null;
        }
        List<V1EnvVar> envVarList = new DecoratedArrayList<V1EnvVar>();
        secretKeys.stream().forEach(each -> {
            V1EnvVar envVar = new V1EnvVar();
            envVar.setName(each);

            V1EnvVarSource envVarSource = new V1EnvVarSource();
            V1SecretKeySelector secretKeySelector = new V1SecretKeySelector();
            secretKeySelector.setName(ManifestResource.SECRET.getName(metaDataContext));
            secretKeySelector.setKey(each);
            envVarSource.setSecretKeyRef(secretKeySelector);
            envVar.setValueFrom(envVarSource);
            envVarList.add(envVar);
        });

        return envVarList;
    }

    private Set<String> getSecretKeys(Secrets secrets) {
        if (secrets == null) {
            return null;
        }
        if (secrets.getSecretsMap() != null && !secrets.getSecretsMap().isEmpty()) {
            return secrets.getSecretsMap().keySet();
        }
        if (secrets.getSecretKeys() != null && !secrets.getSecretKeys().isEmpty()) {
            return secrets.getSecretKeys();
        }
        return null;
    }

    private List<V1EnvVar> getPodSpecEnv(Props props, MetaDataContext metaDataContext) {
        if (props == null || props.getProps().isEmpty()) {
            return null;
        }
        List<V1EnvVar> envVarList = new DecoratedArrayList<V1EnvVar>();
        props.getProps().entrySet().stream().forEach(each -> {
            V1EnvVar envVar = new V1EnvVar();
            envVar.setName(each.getKey());

            V1EnvVarSource envVarSource = new V1EnvVarSource();
            V1ConfigMapKeySelector configMapKeySelector = new V1ConfigMapKeySelector();
            configMapKeySelector.setName(ManifestResource.CONFIG_MAP.getName(metaDataContext));
            configMapKeySelector.setKey(each.getKey());
            envVarSource.setConfigMapKeyRef(configMapKeySelector);
            envVar.setValueFrom(envVarSource);
            envVarList.add(envVar);
        });
        return envVarList;
    }
}
