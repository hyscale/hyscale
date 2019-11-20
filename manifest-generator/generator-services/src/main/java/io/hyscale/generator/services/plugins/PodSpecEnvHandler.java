/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.generator.services.utils.PodSpecEnvUtil;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.DecoratedArrayList;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.model.AppMetaData;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.generator.services.provider.PropsProvider;
import io.hyscale.generator.services.provider.SecretsProvider;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Props;
import io.hyscale.servicespec.commons.model.service.Secrets;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.models.V1EnvVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "PodSpecEnvHandler")
public class PodSpecEnvHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(PodSpecEnvHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        List<ManifestSnippet> snippetList = new ArrayList<>();
        AppMetaData appMetaData = new AppMetaData();
        appMetaData.setAppName(manifestContext.getAppName());
        appMetaData.setEnvName(manifestContext.getEnvName());
        appMetaData.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();

        List<V1EnvVar> envVarList = new DecoratedArrayList<V1EnvVar>();
        try {
            // Preparing Pod Spec env's from props
            Props props = PropsProvider.getProps(serviceSpec);
            if (ManifestPredicates.getPropsPredicate().test(serviceSpec)) {
                logger.debug("Preparing Pod Spec env's from props.");
                envVarList.addAll(getPodSpecEnv(props, appMetaData));
            }

            // Preparing Pod Spec secrets from props
            Secrets secrets = SecretsProvider.getSecrets(serviceSpec);
            if (ManifestPredicates.getSecretsEnvPredicate().test(serviceSpec)) {
                logger.debug("Preparing Pod Spec env's from secrets.");
                envVarList.addAll(getSecretsSnippet(secrets, appMetaData));
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
            logger.error("Error while generating env for Pod spec", e);
        }
        return snippetList;
    }


    private List<V1EnvVar> getSecretsSnippet(Secrets secrets, AppMetaData appMetaData) {
        String secretName = ManifestResource.SECRET.getName(appMetaData);
        return PodSpecEnvUtil.getSecretEnv(secrets,secretName);
    }

    private List<V1EnvVar> getPodSpecEnv(Props props, AppMetaData appMetaData) {
        String configMapName = ManifestResource.CONFIG_MAP.getName(appMetaData);
        return PodSpecEnvUtil.getPropEnv(props,configMapName);
    }
}
