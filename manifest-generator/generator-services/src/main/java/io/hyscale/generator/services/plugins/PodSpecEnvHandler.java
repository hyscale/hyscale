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
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.utils.PodSpecEnvUtil;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.DecoratedArrayList;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.generator.services.provider.PropsProvider;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Props;
import io.hyscale.servicespec.commons.model.service.Secrets;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.openapi.models.V1EnvVar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@ManifestPlugin(name = "PodSpecEnvHandler")
public class PodSpecEnvHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(PodSpecEnvHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        List<ManifestSnippet> snippetList = new ArrayList<>();
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setAppName(manifestContext.getAppName());
        serviceMetadata.setEnvName(manifestContext.getEnvName());
        serviceMetadata.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        String podSpecOwner = ((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));

        List<V1EnvVar> envVarList = new DecoratedArrayList<>();
        try {
            // Preparing Pod Spec env's from props
            Props props = PropsProvider.getProps(serviceSpec);
            if (ManifestPredicates.getPropsPredicate().test(serviceSpec)) {
                logger.debug("Preparing Pod Spec env's from props.");
                envVarList.addAll(getPodSpecEnv(props, serviceMetadata));
            }

            // Preparing Pod Spec secrets from props
            Secrets secrets = serviceSpec.get(HyscaleSpecFields.secrets, Secrets.class);
            if (ManifestPredicates.getSecretsEnvPredicate().test(serviceSpec)) {
                logger.debug("Preparing Pod Spec env's from secrets.");
                envVarList.addAll(getSecretsSnippet(secrets, serviceMetadata));
            }
            if (envVarList.isEmpty()) {
                return Collections.emptyList();
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


    private List<V1EnvVar> getSecretsSnippet(Secrets secrets, ServiceMetadata serviceMetadata) {
        String secretName = ManifestResource.SECRET.getName(serviceMetadata);
        return PodSpecEnvUtil.getSecretEnv(secrets,secretName);
    }

    private List<V1EnvVar> getPodSpecEnv(Props props, ServiceMetadata serviceMetadata) {
        String configMapName = ManifestResource.CONFIG_MAP.getName(serviceMetadata);
        return PodSpecEnvUtil.getPropEnv(props,configMapName);
    }
}
