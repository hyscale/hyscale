package io.hyscale.ctl.controller.plugins;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.component.ComponentInvokerPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.K8sAuthorisation;
import io.hyscale.ctl.commons.models.KubernetesResource;
import io.hyscale.ctl.commons.models.Manifest;
import io.hyscale.ctl.commons.utils.ResourceSelectorUtil;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.ctl.controller.constants.WorkflowConstants;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.deployer.core.model.ResourceKind;
import io.hyscale.ctl.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.ctl.deployer.services.handler.ResourceHandlers;
import io.hyscale.ctl.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.ctl.deployer.services.provider.K8sClientProvider;
import io.hyscale.ctl.deployer.services.util.KubernetesResourceUtil;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1ObjectMeta;

@Component
public class K8sResourcesCleanUpPlugin implements ComponentInvokerPlugin<WorkflowContext> {

	private static final Logger logger = LoggerFactory.getLogger(K8sResourcesCleanUpPlugin.class);

	@Autowired
	private K8sClientProvider clientProvider;

	@Autowired
	private K8sAuthConfigBuilder authConfigBuilder;

	/**
	 * Clean up old resources
	 * 1.	Create map of resources in manifest
	 * 2.	For each Resource where clean up is enabled except PVC:
	 * 		1. Fetch resource from K8s based on selector
	 * 		2. if doesnot exist in map delete
	 */
	@Override
	public void doBefore(WorkflowContext context) throws HyscaleException {
		logger.debug("Starting stale kubernetes resource cleanup");
		ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfigBuilder.getAuthConfig());
		String serviceName = context.getServiceName();
		String appName = context.getAppName();
		String namespace = context.getNamespace();
		String envName = context.getEnvName();
		List<Manifest> manifestList = (List<Manifest>) context.getAttribute(WorkflowConstants.GENERATED_MANIFESTS);

		if (manifestList == null || manifestList.isEmpty()) {
			logger.debug("No resource to cleanup");
			return;
		}
		String selector = ResourceSelectorUtil.getSelector(appName, envName, serviceName);

		try {

			Map<ResourceKind, List<String>> newResourcesMap = getResourcesMap(manifestList);
			boolean isMsgPrinted = false;
			List<ResourceLifeCycleHandler> handlersList = ResourceHandlers.getHandlersList();
			if (handlersList == null) {
				return;
			}
			// Sort handlers based on weight
			List<ResourceLifeCycleHandler> sortedHandlersList = handlersList.stream()
					.sorted((ResourceLifeCycleHandler handler1, ResourceLifeCycleHandler handler2) -> {
						return handler1.getWeight() - handler2.getWeight();
					}).collect(Collectors.toList());

			for (ResourceLifeCycleHandler lifeCycleHandler : sortedHandlersList) {
				if (lifeCycleHandler == null || !lifeCycleHandler.cleanUp()) {
					continue;
				}
				// TODO - Different approach for clean up - dependent resource handling
				if (ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind().equalsIgnoreCase(lifeCycleHandler.getKind())) {
					continue;
				}
				ResourceKind resourceKind = ResourceKind.fromString(lifeCycleHandler.getKind());
				List<String> newResources = newResourcesMap.get(resourceKind) != null
						? newResourcesMap.get(resourceKind)
						: new ArrayList<String>();

				List existingResources = lifeCycleHandler.getBySelector(apiClient, selector, true, namespace);
				if (existingResources == null || existingResources.isEmpty()) {
					continue;
				}

				for (Object existingResource : existingResources) {
					try {
						V1ObjectMeta v1ObjectMeta = KubernetesResourceUtil.getObjectMeta(existingResource);
						String name = v1ObjectMeta.getName();

						if (!newResources.contains(name)) {
							if (!isMsgPrinted) {
								WorkflowLogger.header(ControllerActivity.CLEANING_UP_RESOURCES);
								isMsgPrinted = true;
							}
							lifeCycleHandler.delete(apiClient, name, namespace, true);
						}
					} catch (Exception e) {
						// Ignore error and continue
						logger.error("Error while cleaning up resource: {}, error: {}", resourceKind.getKind(),
								e.getMessage());
					}
				}

			}
		} catch (Exception e) {
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_READ_MANIFEST);
			logger.error("Error while cleaning stale kubernetes resources, error: {}", ex.getMessage());
			return;
		}
	}

	@Override
	public void doAfter(WorkflowContext context) throws HyscaleException {

	}

	@Override
	public void onError(WorkflowContext context, Throwable th) {
		logger.error("Error while cleaning up stale resources, error {}", th.getMessage());
	}

	private Map<ResourceKind, List<String>> getResourcesMap(List<Manifest> manifestList)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, IOException {
		Map<ResourceKind, List<String>> resourcesMap = new HashMap<ResourceKind, List<String>>();
		for (Manifest manifest : manifestList) {
			KubernetesResource k8sResource = KubernetesResourceUtil.getKubernetesResource(manifest, null);
			if (k8sResource == null || k8sResource.getV1ObjectMeta() == null) {
				continue;
			}
			ResourceKind resourceKind = ResourceKind.fromString(k8sResource.getKind());
			if (resourceKind == null) {
				continue;
			}
			if (resourcesMap.get(resourceKind) == null) {
				resourcesMap.put(resourceKind, new ArrayList<String>());
			}
			resourcesMap.get(resourceKind).add(k8sResource.getV1ObjectMeta().getName());
		}

		return resourcesMap;
	}

}
