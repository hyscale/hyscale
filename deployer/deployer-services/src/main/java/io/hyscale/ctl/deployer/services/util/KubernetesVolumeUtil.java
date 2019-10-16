package io.hyscale.ctl.deployer.services.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.deployer.core.model.ResourceKind;
import io.hyscale.ctl.deployer.services.handler.ResourceHandlers;
import io.hyscale.ctl.deployer.services.handler.impl.V1PodHandler;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1Volume;

/**
 * Utility to get volume related information from resource
 *
 */
public class KubernetesVolumeUtil {

	public static Set<String> getPodVolumes(ApiClient apiClient, String selector, String namespace)
			throws HyscaleException {
		V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());

		List<V1Pod> podsList = podHandler.getBySelector(apiClient, selector, true, namespace);

		return getPodVolumes(podsList);

	}

	public static Set<String> getPodVolumes(List<V1Pod> podsList) {
		Set<String> podsVolumes = new HashSet<String>();
		if (podsList == null || podsList.isEmpty()) {
			return podsVolumes;
		}
		podsList.stream().forEach(pod -> {
			V1PodSpec podSpec = pod.getSpec();
			if (podSpec == null) {
				return;
			}
			List<V1Volume> volumes = podSpec.getVolumes();
			if (volumes == null || volumes.isEmpty()) {
				return;
			}
			volumes.stream().forEach(volume -> {
				V1PersistentVolumeClaimVolumeSource podPVC = volume.getPersistentVolumeClaim();
				if (podPVC != null) {
					podsVolumes.add(podPVC.getClaimName());
				}
			});
		});

		return podsVolumes;
	}
	
}
