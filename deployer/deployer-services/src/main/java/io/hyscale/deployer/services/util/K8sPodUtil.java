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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.deployer.core.model.ReplicaInfo;
import io.hyscale.deployer.services.model.PodCondition;
import io.kubernetes.client.models.V1ContainerStatus;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodCondition;

/**
 * Created by sameerag on 12/9/18.
 * Utility for K8s pod level information
 * 
 */
public class K8sPodUtil {
    
    public static List<ReplicaInfo> getReplicaInfo(List<V1Pod> podList) {
        if (podList == null) {
            return null;
        }
        return podList.stream().map(each -> getReplicaInfo(each)).collect(Collectors.toList());
    }
    
    public static ReplicaInfo getReplicaInfo(V1Pod pod) {
        if (pod == null) {
            return null;
        }
        ReplicaInfo replicaInfo = new ReplicaInfo();
        replicaInfo.setName(pod.getMetadata().getName());
        replicaInfo.setAge(pod.getStatus().getStartTime());
        replicaInfo.setStatus(getAggregatedStatusOfContainersForPod(pod));
        
        return replicaInfo;
    }

	/**
	 * Gets aggregate status from Init containers - empty if init containers are ready
	 * If init container status not found gets aggregated status from containers
	 * If containers status not found get pod level status
	 * @param v1Pod
	 * @return Status from containers in pod 
	 */
	public static String getAggregatedStatusOfContainersForPod(V1Pod v1Pod) {
		if (v1Pod == null) {
			return null;
		}
		String initContainerAggStatus = validateAndGetInitContainerStatuses(
				v1Pod.getStatus().getInitContainerStatuses());
		if (initContainerAggStatus != null) {
			return initContainerAggStatus;
		}
		String containerAggStatus = validateAndGetContainerStatuses(v1Pod.getStatus().getContainerStatuses());
		return containerAggStatus != null ? containerAggStatus : v1Pod.getStatus().getPhase();
	}

	/**
	 * Failure Reason order: Initialized | Pod Scheduled | Containers Ready | Ready
	 */
	public static String getFailureMessage(V1Pod v1Pod) {
		if (v1Pod == null || v1Pod.getStatus() == null) {
			return null;
		}
		List<V1PodCondition> podConditions = v1Pod.getStatus().getConditions();
		if (podConditions == null || podConditions.isEmpty()) {
			return null;
		}
		if (checkForPodCondition(v1Pod, PodCondition.READY)) {
			return null;
		}
		Map<PodCondition, V1PodCondition> podConditionsMap = new HashMap<PodCondition, V1PodCondition>();
		podConditions.stream().forEach(condition -> {
			PodCondition podCondition = PodCondition.fromString(condition.getType());
			if (podCondition != null) {
				podConditionsMap.put(podCondition, condition);
			}
		});

		String message = getMessageForCondition(podConditionsMap, PodCondition.INITIALIZED);
		if (StringUtils.isNotBlank(message)) {
			return message;
		}
		message = getMessageForCondition(podConditionsMap, PodCondition.POD_SCHEDULED);
		if (StringUtils.isNotBlank(message)) {
			return message;
		}
		message = getMessageForCondition(podConditionsMap, PodCondition.CONTAINERS_READY);
		if (StringUtils.isNotBlank(message)) {
			return message;
		}
		message = getMessageForCondition(podConditionsMap, PodCondition.READY);

		return message;
	}

	private static String getMessageForCondition(Map<PodCondition, V1PodCondition> podConditionsMap,
			PodCondition condition) {
		V1PodCondition podCondition = podConditionsMap.get(condition);
		if (podCondition != null && podCondition.getStatus() != null && !Boolean.valueOf(podCondition.getStatus())) {
			return podCondition.getMessage();
		}

		return null;
	}

	/**
	 * 
	 * @param containerStatuses
	 * @return status of container not in running state
	 */
	private static String validateAndGetContainerStatuses(List<V1ContainerStatus> containerStatuses) {
		if (containerStatuses == null || containerStatuses.isEmpty()) {
			return null;
		}
		String aggregateStatus = null;
		for (V1ContainerStatus each : containerStatuses) {
			if (each.getLastState() != null) {
				if (each.getLastState().getTerminated() != null) {
					aggregateStatus = each.getLastState().getTerminated().getReason();
					break;
				} else if (each.getLastState().getWaiting() != null) {
					aggregateStatus = each.getLastState().getWaiting().getReason();
					break;
				}
			}
			if (each.getState().getRunning() == null) {
				if (each.getState().getTerminated() != null) {
					aggregateStatus = each.getState().getTerminated().getReason();
					break;
				} else if (each.getState().getWaiting() != null) {
					aggregateStatus = each.getState().getWaiting().getReason();
					break;
				}
			}
		}
		return aggregateStatus;
	}

	public static String getContainerStatus(V1ContainerStatus containerStatus) {
		if (containerStatus.getState().getRunning() == null) {
			if (containerStatus.getState().getTerminated() != null) {
				return containerStatus.getState().getTerminated().getReason();
			} else if (containerStatus.getState().getWaiting() != null) {
				return containerStatus.getState().getWaiting().getReason();
			}
		} else {
			// any other way?
			return "Running";
		}
		return null;
	}

	/**
	 * 
	 * @param initContainerStatuses
	 * @return status of init container in waiting state, else null
	 */
	private static String validateAndGetInitContainerStatuses(List<V1ContainerStatus> initContainerStatuses) {
		if (initContainerStatuses == null || initContainerStatuses.isEmpty()) {
			return null;
		}
		String initContainerStatus = null;
		for (V1ContainerStatus each : initContainerStatuses) {
			if (each.getState().getTerminated() != null && each.isReady()) {
				continue;
			} else if (each.getState().getWaiting() != null) {
				initContainerStatus = each.getState().getWaiting().getReason();
				break;
			} else if (each.getState().getRunning() != null) {
				// TODO Handle
			}
		}
		return initContainerStatus;
	}

	// If any container restart > 0 or pod in errorState
	public static boolean checkForPodCreation(V1Pod v1Pod) {
		String status = getAggregatedStatusOfContainersForPod(v1Pod);
		if (K8SRuntimeConstants.POD_RUNING_STATE_CONDITION.equalsIgnoreCase(status)) {
			return true;
		}
		if (checkForContainersRestart(v1Pod.getStatus().getContainerStatuses())
				|| K8SRuntimeConstants.POD_ERROR_SATES.contains(status)) {
			return false;
		}
		return false;
	}

	public static boolean checkForContainersRestart(List<V1ContainerStatus> containerStatuses) {
		if (containerStatuses == null) {
			return false;
		}

		for (V1ContainerStatus status : containerStatuses) {
			Integer contRestartCount = status.getRestartCount();
			if (contRestartCount != null && contRestartCount > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if provided pod condition status is true in the pod
	 * @param pod
	 * @param podCondition
	 * @return true is condition is set in Pod, else false
	 */
	public static boolean checkForPodCondition(V1Pod pod, PodCondition podCondition) {
		List<V1PodCondition> conditions = pod.getStatus().getConditions();
		if (conditions == null || conditions.isEmpty() || podCondition == null) {
			return false;
		}
		for (V1PodCondition v1PodCondition : conditions) {
			if (v1PodCondition.getType().equalsIgnoreCase(podCondition.getPodCondition())
					&& Boolean.valueOf(v1PodCondition.getStatus())) {
				return true;
			}
		}
		return false;
	}

	public static String getPodMessage(V1Pod v1Pod) {
		if (v1Pod == null) {
			return null;
		}
		String initContainerAggStatusMsg = validateAndGetInitContainerMessage(
				v1Pod.getStatus().getInitContainerStatuses());
		if (initContainerAggStatusMsg != null) {
			return initContainerAggStatusMsg;
		}
		String containerAggStatusMsg = validateAndGetContainerStatusMessage(v1Pod.getStatus().getContainerStatuses());
		return containerAggStatusMsg != null ? containerAggStatusMsg : v1Pod.getStatus().getMessage();
	}

	private static String validateAndGetContainerStatusMessage(List<V1ContainerStatus> containerStatuses) {
		if (containerStatuses == null || containerStatuses.isEmpty()) {
			return null;
		}
		String aggregateStatus = null;
		for (V1ContainerStatus each : containerStatuses) {
			if (each.getLastState() != null) {
				if (each.getLastState().getTerminated() != null) {
					aggregateStatus = each.getLastState().getTerminated().getMessage();
					break;
				} else if (each.getLastState().getWaiting() != null) {
					aggregateStatus = each.getLastState().getWaiting().getMessage();
					break;
				}
			}
			if (each.getState().getRunning() == null) {
				if (each.getState().getTerminated() != null) {
					aggregateStatus = each.getState().getTerminated().getMessage();
					break;
				} else if (each.getState().getWaiting() != null) {
					aggregateStatus = each.getState().getWaiting().getMessage();
					break;
				}
			}
		}
		return aggregateStatus;
	}

	private static String validateAndGetInitContainerMessage(List<V1ContainerStatus> initContainerStatuses) {
		if (initContainerStatuses == null || initContainerStatuses.isEmpty()) {
			return null;
		}
		String initContainerStatus = null;
		for (V1ContainerStatus each : initContainerStatuses) {
			if (each.getState().getTerminated() != null && each.isReady()) {
				continue;
			} else if (each.getState().getWaiting() != null) {
				initContainerStatus = each.getState().getWaiting().getMessage();
				break;
			} else if (each.getState().getRunning() != null) {
				// TODO
			}
		}
		return initContainerStatus;
	}
}
