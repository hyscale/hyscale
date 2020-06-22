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
package io.hyscale.deployer.services.handler.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.VersionApi;
import io.kubernetes.client.openapi.models.VersionInfo;

/**
 * Class to provide kubernetes cluster version details
 *
 */
@Component
public class K8sVersionHandler {

    private static final Logger logger = LoggerFactory.getLogger(K8sVersionHandler.class);
    
    public VersionInfo getVersion(ApiClient apiClient) throws HyscaleException {
        if (apiClient == null) {
            return null;
        }
        VersionApi versionApi = new VersionApi(apiClient);
        try {
            return versionApi.getCode();
        } catch (ApiException e) {
            logger.error("Error while fetching cluster version", e);
            throw new HyscaleException(DeployerErrorCodes.FAILED_TO_GET_RESOURCE, getKind());
        }
    }
    
    public String getKind() {
        return ResourceKind.VERSION.getKind();
    }
}
