package io.hyscale.deployer.services.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ResourceLabelUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.impl.V1PodHandler;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1Volume;

/**
 * Utility to get volume related information from resource
 *
 */
public class KubernetesVolumeUtil {

	public static Map<String, Set<String>> getServiceVolNamesFromPVC(List<V1PersistentVolumeClaim> pvcList) {
		if (pvcList == null || pvcList.isEmpty()) {
			return null;
		}

		Map<String, Set<String>> serviceVolumes = new HashMap<String, Set<String>>();

		pvcList.stream().forEach(pvc -> {
			Map<String, String> labels = pvc.getMetadata().getLabels();
			String serviceName = ResourceLabelUtil.getServiceFromLabel(labels);
			if (serviceVolumes.get(serviceName) == null) {
				serviceVolumes.put(serviceName, new HashSet<String>());
			}
			serviceVolumes.get(serviceName).add(getVolumeNameFromPVC(pvc));
		});

		return serviceVolumes;
	}

	public static Map<String, Set<String>> getServicePVCs(List<V1PersistentVolumeClaim> pvcList) {
		if (pvcList == null || pvcList.isEmpty()) {
			return null;
		}

		Map<String, Set<String>> servicePVCs = new HashMap<String, Set<String>>();
		pvcList.stream().forEach(pvc -> {
			String serviceName = ResourceLabelUtil.getServiceFromLabel(pvc.getMetadata().getLabels());

			if (servicePVCs.get(serviceName) == null) {
				servicePVCs.put(serviceName, new HashSet<String>());
			}
			servicePVCs.get(serviceName).add(pvc.getMetadata().getName());
		});

		return servicePVCs;
	}

	/**
	 * Get volume name from pvc
	 * pvc name = volume_name-app_name-service_name-index
	 * @param pvcName
	 * @return volume_name
	 */
	public static String getVolumeNameFromPVC(V1PersistentVolumeClaim pvc) {
		if (pvc == null) {
			return null;
		}
		String pvcName = pvc.getMetadata().getName();
		Map<String, String> labels = pvc.getMetadata().getLabels();
		String appName = ResourceLabelUtil.getAppFromLabel(labels);
		String serviceName = ResourceLabelUtil.getServiceFromLabel(labels);

		if (StringUtils.isBlank(appName) || StringUtils.isBlank(serviceName)) {
			return pvcName;
		}

		String suffix = appName + ToolConstants.DASH + serviceName;

		return pvcName.substring(0, pvcName.indexOf(suffix) - 1);
	}

	public static Set<String> getPodVolumes(ApiClient apiClient, String selector, String namespace)
			throws HyscaleException {
		V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());

		List<V1Pod> podsList = podHandler.getBySelector(apiClient, selector, true, namespace);

		return getPodsVolumes(podsList);

	}

	public static Set<String> getPodsVolumes(List<V1Pod> podsList) {
		Set<String> podsVolumes = new HashSet<String>();
		if (podsList == null || podsList.isEmpty()) {
			return podsVolumes;
		}
		podsList.stream().forEach(pod -> {
			podsVolumes.addAll(getPodVolumes(pod));
		});
		return podsVolumes;
	}

	public static Set<String> getPodVolumes(V1Pod pod) {
		Set<String> podsVolumes = new HashSet<String>();
		if (pod == null) {
			return null;
		}
		V1PodSpec podSpec = pod.getSpec();
		if (podSpec == null) {
			return podsVolumes;
		}
		List<V1Volume> volumes = podSpec.getVolumes();
		if (volumes == null || volumes.isEmpty()) {
			return podsVolumes;
		}
		volumes.stream().forEach(volume -> {
			V1PersistentVolumeClaimVolumeSource podPVC = volume.getPersistentVolumeClaim();
			if (podPVC != null) {
				podsVolumes.add(podPVC.getClaimName());
			}
		});
		return podsVolumes;
	}

}
