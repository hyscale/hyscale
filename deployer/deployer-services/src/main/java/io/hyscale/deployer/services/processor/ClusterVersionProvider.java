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
package io.hyscale.deployer.services.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ClusterVersionInfo;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.deployer.services.handler.impl.K8sVersionHandler;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.VersionInfo;

@Component
public class ClusterVersionProvider {
    
    @Autowired
    private K8sClientProvider clientProvider;
    
    @Autowired
    private K8sVersionHandler k8sVersionHandler;
    
    private ClusterVersionInfo version;
    
    public ClusterVersionInfo getVersion(K8sAuthorisation authConfig) throws HyscaleException {
        if (version != null) {
            return version;
        }
        if (authConfig == null) {
            return null;
        }
        return getVersion(clientProvider.get(authConfig));
    }
    
    public ClusterVersionInfo getVersion(ApiClient apiClient) throws HyscaleException {
        if (version != null) {
            return version;
        }
        if (apiClient == null) {
            return null;
        }
        return getVersion(k8sVersionHandler.getVersion(apiClient));
    }

    public ClusterVersionInfo getVersion(VersionInfo versionInfo) {
        if (version != null) {
            return version;
        }
        if (versionInfo == null) {
            return null;
        }
        version = new ClusterVersionInfo();
        version.setMajor(versionInfo.getMajor());
        version.setMinor(versionInfo.getMinor());
        return version;
    }
    
}
