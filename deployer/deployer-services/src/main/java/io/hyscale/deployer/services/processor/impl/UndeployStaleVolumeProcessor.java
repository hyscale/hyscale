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
package io.hyscale.deployer.services.processor.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.impl.V1PersistentVolumeClaimHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.processor.DeployerInterceptorProcessor;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.deployer.services.util.KubernetesVolumeUtil;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1PersistentVolumeClaim;

/**
 * Processor to handle volumes post undeployment
 * Lists all the volumes left in cluster after the undeployment
 * @author tushar
 *
 */
@Component
public class UndeployStaleVolumeProcessor extends DeployerInterceptorProcessor {
    private static final Logger logger = LoggerFactory.getLogger(UndeployStaleVolumeProcessor.class);

    @Autowired
    private K8sClientProvider clientProvider;

    @Override
    protected void _preProcess(DeploymentContext context) throws HyscaleException {
    }

    /**
     * Get PVCs
     * Mark all pvcs as stale resources
     */
    @Override
    protected void _postProcess(DeploymentContext context) throws HyscaleException {
        String serviceName = context.getServiceName();
        String appName = context.getAppName();
        String namespace = context.getNamespace();
        String envName = context.getEnvName();

        ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());

        V1PersistentVolumeClaimHandler pvcHandler = (V1PersistentVolumeClaimHandler) ResourceHandlers
                .getHandlerOf(ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());

        String selector = ResourceSelectorUtil.getSelector(appName, envName, serviceName);

        List<V1PersistentVolumeClaim> pvcItemsList = pvcHandler.getBySelector(apiClient, selector, true, namespace);
        if (pvcItemsList == null || pvcItemsList.isEmpty()) {
            return;
        }

        Map<String, Set<String>> serviceVsVolumes = KubernetesVolumeUtil.getServiceVolumeNames(pvcItemsList);

        Map<String, Set<String>> serviceVsPVC = KubernetesVolumeUtil.getServicePVCs(pvcItemsList);

        serviceVsVolumes.entrySet().stream().forEach(entity -> {
            WorkflowLogger.persist(DeployerActivity.STALE_VOLUME_REUSE, entity.getValue().toString(), namespace,
                    serviceVsPVC.get(entity.getKey()).toString(), entity.getKey());
        });
    }

    @Override
    protected void _onError(DeploymentContext context, Throwable th) throws HyscaleException {
        if (th != null && th instanceof HyscaleException) {
            HyscaleException hex = (HyscaleException) th;
            logger.error("Inside on error method in {}", getClass().toString(), hex.getMessage());
            throw hex;
        }
    }

}
