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
import io.hyscale.generator.services.utils.SecretsDataUtil;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.utils.HyscaleFilesUtil;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.model.AppMetaData;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Secrets;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "ConfigMapDataHandler")
public class SecretsDataHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(SecretsDataHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        if (serviceSpec == null) {
            throw new HyscaleException(CommonErrorCode.SERVICE_SPEC_REQUIRED);
        }
        if (manifestContext == null) {
            throw new HyscaleException(ManifestErrorCodes.CONTEXT_REQUIRED);
        }
        Secrets secrets = serviceSpec.get(HyscaleSpecFields.secrets, Secrets.class);
        if (!ManifestResource.SECRET.getPredicate().test(serviceSpec)) {
            return null;
        }

        String secretsVolumePath = serviceSpec.get(HyscaleSpecFields.secretsVolumePath, String.class);
        AppMetaData appMetaData = new AppMetaData();
        appMetaData.setAppName(manifestContext.getAppName());
        appMetaData.setEnvName(manifestContext.getEnvName());
        appMetaData.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));

        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        try {

            manifestSnippetList.add(getSecretsData(secrets, secretsVolumePath));
        } catch (JsonProcessingException e) {
            logger.error("Error while generating manifest for props of service {}", appMetaData.getServiceName(), e);
        }
        return manifestSnippetList;

    }

    private ManifestSnippet getSecretsData(Secrets secrets, String secretsVolumePath)
            throws JsonProcessingException {

        String fileName = null;
        try{
            fileName = HyscaleFilesUtil.getFileName(secretsVolumePath);
        }catch (HyscaleException e){
            logger.error("Error while processing secrets volumes path {}.", secretsVolumePath);
        }
        ManifestSnippet snippet = SecretsDataUtil.build(secrets,secretsVolumePath,fileName);
        return snippet;
    }
}
