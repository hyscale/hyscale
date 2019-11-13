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
package io.hyscale.deployer.services.model;


import io.hyscale.commons.models.K8sAuthType;
import io.hyscale.commons.models.K8sAuthorisation;
import io.kubernetes.client.util.KubeConfig;

public class K8sKubeConfigAuth implements K8sAuthorisation {

    private KubeConfig kubeConfig;

    public KubeConfig getKubeConfig() { return kubeConfig; }

    public void setKubeConfig(KubeConfig kubeConfig) { this.kubeConfig = kubeConfig; }

    @Override
    public K8sAuthType getK8sAuthType() {
        return K8sAuthType.KUBE_CONFIG_OBJECT;
    }

}
