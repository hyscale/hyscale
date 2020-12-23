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
package io.hyscale.deployer.services.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ResourceLabelUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.impl.V1PodHandler;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1Volume;

/**
 * Utility to get volume related information from resource
 *
 */
public class KubernetesVolumeUtil {
    
    private KubernetesVolumeUtil() {}

	public static Map<String, Set<String>> getServiceVolumeNames(List<V1PersistentVolumeClaim> pvcList) {
		if (pvcList == null || pvcList.isEmpty()) {
			return null;
		}

		Map<String, Set<String>> serviceVolumes = new HashMap<>();

		pvcList.stream().forEach(pvc -> {
			Map<String, String> labels = pvc.getMetadata().getLabels();
			String serviceName = ResourceLabelUtil.getServiceName(labels);
			if (serviceVolumes.get(serviceName) == null) {
				serviceVolumes.put(serviceName, new HashSet<>());
			}
			serviceVolumes.get(serviceName).add(getVolumeName(pvc));
		});

		return serviceVolumes;
	}

	public static Map<String, Set<String>> getServicePVCs(List<V1PersistentVolumeClaim> pvcList) {
		if (pvcList == null || pvcList.isEmpty()) {
			return null;
		}

		Map<String, Set<String>> servicePVCs = new HashMap<>();
		pvcList.stream().forEach(pvc -> {
			String serviceName = ResourceLabelUtil.getServiceName(pvc.getMetadata().getLabels());

			if (servicePVCs.get(serviceName) == null) {
				servicePVCs.put(serviceName, new HashSet<>());
			}
			servicePVCs.get(serviceName).add(pvc.getMetadata().getName());
		});

		return servicePVCs;
	}

	/**
	 * Get volume name from pvc
	 * pvc name = volume_name-service_name-index
	 * @param pvc
	 * @return volumeName
	 */
	public static String getVolumeName(V1PersistentVolumeClaim pvc) {
		if (pvc == null) {
			return null;
		}
		String pvcName = pvc.getMetadata().getName();
		Map<String, String> labels = pvc.getMetadata().getLabels();
		String volName = ResourceLabelUtil.getVolumeName(labels);
		if (StringUtils.isNotBlank(volName)) {
		    return volName;
		}
		String appName = ResourceLabelUtil.getAppName(labels);
		String serviceName = ResourceLabelUtil.getServiceName(labels);

		if (StringUtils.isBlank(appName) || StringUtils.isBlank(serviceName)) {
			return pvcName;
		}

		int indexOfSuffix = pvcName.indexOf(serviceName);
		
		if (indexOfSuffix > 0) {
		    return pvcName.substring(0, indexOfSuffix - 1);
		}
		
		return pvcName;
	}

	public static Set<String> getPodVolumes(ApiClient apiClient, String selector, String namespace)
			throws HyscaleException {
		V1PodHandler podHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());

		List<V1Pod> podsList = podHandler.getBySelector(apiClient, selector, true, namespace);

		return getPodsVolumes(podsList);

	}

	public static Set<String> getPodsVolumes(List<V1Pod> podsList) {
		Set<String> podsVolumes = new HashSet<>();
		if (podsList == null || podsList.isEmpty()) {
			return podsVolumes;
		}
		podsList.stream().forEach(pod -> podsVolumes.addAll(getPodVolumes(pod)));
		return podsVolumes;
	}

	public static Set<String> getPodVolumes(V1Pod pod) {
		Set<String> podsVolumes = new HashSet<>();
		if (pod == null) {
			return Collections.emptySet();
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
