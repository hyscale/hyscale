package io.hyscale.ctl.controller.plugins;

import java.util.List;
import java.util.Set;

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
import io.hyscale.ctl.deployer.services.handler.impl.V1PersistentVolumeClaimHandler;
import io.hyscale.ctl.deployer.services.provider.K8sClientProvider;
import io.hyscale.ctl.deployer.services.util.KubernetesResourceUtil;
import io.hyscale.ctl.deployer.services.util.KubernetesVolumeUtil;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1PersistentVolumeClaim;

/**
 * Plugin to clean up stale volumes on the cluster
 * @author tushart
 *
 */
@Component
public class VolumeCleanUpPlugin implements ComponentInvokerPlugin<WorkflowContext> {

	private static final Logger logger = LoggerFactory.getLogger(VolumeCleanUpPlugin.class);

	@Autowired
	private K8sClientProvider clientProvider;

	@Autowired
	private K8sAuthConfigBuilder authConfigBuilder;

	@Override
	public void doBefore(WorkflowContext context) throws HyscaleException {

	}

	@Override
	public void doAfter(WorkflowContext context) throws HyscaleException {

		ApiClient apiClient = clientProvider.get((K8sAuthorisation) authConfigBuilder.getAuthConfig());
		String serviceName = context.getServiceName();
		String appName = context.getAppName();
		String namespace = context.getNamespace();
		String envName = context.getEnvName();
		List<Manifest> mainfestList = (List<Manifest>) context.getAttribute(WorkflowConstants.GENERATED_MANIFESTS);
		if (mainfestList == null || mainfestList.isEmpty()) {
			logger.debug("No resource to cleanup");
			return;
		}
		String selector = ResourceSelectorUtil.getSelector(appName, envName, serviceName);
		for (Manifest manifest : mainfestList) {
			try {
				KubernetesResource k8sResource = KubernetesResourceUtil.getKubernetesResource(manifest, namespace);
				ResourceLifeCycleHandler lifeCycleHandler = ResourceHandlers.getHandlerOf(k8sResource.getKind());
				if (lifeCycleHandler != null) {
					if (ResourceKind.STATEFUL_SET.getKind().equalsIgnoreCase(lifeCycleHandler.getKind())) {
						cleanUpOldVolumes(false, apiClient, selector, namespace);
					} else if (ResourceKind.DEPLOYMENT.getKind().equalsIgnoreCase(lifeCycleHandler.getKind())) {
						// Delete all pvcs
						cleanUpOldVolumes(true, apiClient, selector, namespace);
					}
				}
			} catch (Exception e) {
				HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_READ_MANIFEST);
				logger.error("Error while cleaning old pvcs", ex);
				return;
			}
		}
	}

	@Override
	public void onError(WorkflowContext context, Throwable th) {
		logger.error("Error while cleaning up stale volumes, error {}", th.getMessage());
	}

	/**
	 * 1. Delete All - based on selector
	 * 2. Fetch pods based on selector
	 * 		Create list of pvc from pods
	 * 		Fetch pvc based on selector
	 * 		Delete pvc not found in previous list
	 * @param deleteAll
	 * @param apiClient
	 * @param selector
	 * @param namespace
	 */
	private void cleanUpOldVolumes(boolean deleteAll, ApiClient apiClient, String selector, String namespace) {
		try {
			V1PersistentVolumeClaimHandler pvcHandler = (V1PersistentVolumeClaimHandler) ResourceHandlers
					.getHandlerOf(ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());

			List<V1PersistentVolumeClaim> pvcItemsList = pvcHandler.getBySelector(apiClient, selector, true, namespace);
			if (pvcItemsList == null || pvcItemsList.isEmpty()) {
				return;
			}

			if (deleteAll) {
				printCleaningMsg();
				deleteAllPVC(pvcHandler, apiClient, namespace, pvcItemsList);
				return;
			}
			Set<String> podsVolumes = KubernetesVolumeUtil.getPodVolumes(apiClient, selector, namespace);

			if (podsVolumes == null || podsVolumes.isEmpty()) {
				printCleaningMsg();
				deleteAllPVC(pvcHandler, apiClient, namespace, pvcItemsList);
				return;
			}
			boolean msgPrinted = false;
			for (V1PersistentVolumeClaim pvc : pvcItemsList) {
				String pvcName = pvc.getMetadata().getName();
				if (!podsVolumes.contains(pvcName)) {
					if (!msgPrinted) {
						printCleaningMsg();
						msgPrinted = true;
					}
					try {
						logger.debug("Deleting PVC: {} in namespace: {}", pvcName, namespace);
						pvcHandler.delete(apiClient, pvcName, namespace, false);
					} catch (HyscaleException e) {
						logger.error("Error while deleting pvc: {}, ignoring", pvcName);
					}
				}
			}
		} catch (HyscaleException e) {
			logger.error("Error while cleaning up pvcs, error {}", e.getMessage());
			return;
		}

	}

	private void deleteAllPVC(V1PersistentVolumeClaimHandler pvcHandler, ApiClient apiClient, String namespace,
			List<V1PersistentVolumeClaim> pvcItemsList) {
		pvcItemsList.stream().forEach(pvc -> {
			String name = pvc.getMetadata().getName();
			try {
				pvcHandler.delete(apiClient, name, namespace, false);
			} catch (HyscaleException e) {
				logger.error("Error while deleting PVC: {}, error: {}, ignoring", name, e.getMessage());
			}
		});
	}

	private void printCleaningMsg() {
		WorkflowLogger.header(ControllerActivity.CLEANING_UP_VOLUMES);
	}

}
