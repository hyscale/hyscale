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
package io.hyscale.controller.hooks;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.component.InvokerHook;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.impl.V1PersistentVolumeClaimHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.deployer.services.util.KubernetesVolumeUtil;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;

/**
 * To get a list of pvc that will no longer be used after
 * this undeployment
 * @author tushart
 *
 */
@Component
public class StaleVolumeDetailsHook implements InvokerHook<WorkflowContext> {

	private static final Logger logger = LoggerFactory.getLogger(StaleVolumeDetailsHook.class);

	@Autowired
	private K8sClientProvider clientProvider;

	@Autowired
	private K8sAuthConfigBuilder authConfigBuilder;

	@Override
	public void preHook(WorkflowContext context) throws HyscaleException {

	}

	/**
	 * Get PVCs
	 * Mark all pvcs as stale resources
	 */
	@Override
	public void postHook(WorkflowContext context) throws HyscaleException {
		String serviceName = context.getServiceName();
		String appName = context.getAppName();
		String namespace = context.getNamespace();
		String envName = context.getEnvName();

		ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfigBuilder.getAuthConfig());

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
	public void onError(WorkflowContext context, Throwable th) {
		logger.debug("Error while getting stale pvc, ignoring", th);
	}

}
