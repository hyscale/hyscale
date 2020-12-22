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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import io.hyscale.deployer.services.model.PodStatus;
import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.deployer.services.model.PodCondition;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;

/**
 * Created by sameerag on 12/9/18.
 * Utility for K8s pod level information
 *
 */
public class K8sPodUtil {

    private K8sPodUtil() {}
    /**
     * Gets aggregate status from Init containers - empty if init containers are ready
     * If init container status not found gets aggregated status from containers
     * If containers status not found get pod level status
     * @param v1Pod
     * @return ServiceStatus from containers in pod
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
        if(v1Pod.getMetadata().getDeletionTimestamp() != null){
            return PodStatus.TERMINATING.getStatus();
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
        Map<PodCondition, V1PodCondition> podConditionsMap = new EnumMap<>(PodCondition.class);
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
            if (!each.getReady() && each.getLastState() != null) {
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
            if (each.getState().getTerminated() != null && each.getReady()) {
                // Do nothing
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
        if (v1Pod == null) {
            return false;
        }
        String status = getAggregatedStatusOfContainersForPod(v1Pod);
        if (K8SRuntimeConstants.POD_RUNING_STATE_CONDITION.equalsIgnoreCase(status)) {
            return true;
        }
        return checkForContainersRestart(v1Pod.getStatus().getContainerStatuses())
                || K8SRuntimeConstants.POD_ERROR_SATES.contains(status);
    }
    
    /**
     * @param v1Pod
     * @return return true if pod is in failed state, else false
     */
	public static boolean checkForPodFailure(V1Pod v1Pod) {
		return K8SRuntimeConstants.POD_ERROR_SATES.contains(getAggregatedStatusOfContainersForPod(v1Pod));
	}


    /**
     * @param pod
     * @param restartPodCount
     * @return If pod restart count is greater than to given restartPodCount then it will return true, else false
     */
	public static boolean checkForPodRestart(V1Pod pod, Long restartPodCount) {
		if(pod==null ||pod.getStatus()==null||pod.getStatus().getContainerStatuses()==null) {
			return false;
		}
		return pod.getStatus().getContainerStatuses().stream().anyMatch(s -> s.getRestartCount() > restartPodCount);

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
            if (each.getState().getTerminated() != null && each.getReady()) {
                // Do nothing
            } else if (each.getState().getWaiting() != null) {
                initContainerStatus = each.getState().getWaiting().getMessage();
                break;
            } else if (each.getState().getRunning() != null) {
                // TODO
            }
        }
        return initContainerStatus;
    }

    /**
     *
     * @param podList
     * @return pod owner kind if all pods have same owner, else null
     */

    public static String getPodsUniqueOwner(List<V1Pod> podList) {
        if (podList == null || podList.isEmpty()) {
            return null;
        }

        String podOwner = getPodOwner(podList.get(0));
        if (StringUtils.isBlank(podOwner)) {
            return null;
        }

        for (V1Pod pod : podList) {
            if (!podOwner.equals(getPodOwner(pod))) {
                return null;
            }
        }

        return podOwner;
    }

    /**
     *
     * @param pod
     * @return pod owner kind
     */
    public static String getPodOwner(V1Pod pod) {
        if (pod == null) {
            return null;
        }
        V1OwnerReference podOwner = getOwnerReference(pod);

        return podOwner != null ? podOwner.getKind() : null;
    }
  
    /**
     *
     * @param podList
     * @param filter predicate used for filtering
     * @return filtered pods
     */
    public static List<V1Pod> filterPods(List<V1Pod> podList, Predicate<V1Pod> filter) {
        if (podList == null || podList.isEmpty() || filter == null) {
            return podList;
        }
        return podList.stream().filter(filter::test).collect(Collectors.toList());
    }

    /**
     *
     * @param podList
     * @param filter condition and value
     * @param filterValue - value used for filtering
     * @return
     */
    public static List<V1Pod> filterPods(List<V1Pod> podList, BiPredicate filter, Object filterValue) {
        if (podList == null || podList.isEmpty() || filter == null || filterValue == null) {
            return podList;
        }

        return podList.stream().filter(pod -> filter.test(pod, filterValue))
                .collect(Collectors.toList());
    }

    /**
     * Check if pods contains passed labels(key as well as value)
     * @param pod
     * @param labels
     * @return true if pod contains passed labels, else false
     */
    public static boolean checkPodLabels(V1Pod pod, Map<String, String> labels) {
        if (pod == null) {
            return false;
        }
        if (labels == null || labels.isEmpty()) {
            return true;
        }
        Map<String, String> podLabels = pod.getMetadata().getLabels();

        if (podLabels == null) {
            return false;
        }

        return podLabels.entrySet().containsAll(labels.entrySet());
    }

    /**
     * Pods are ambiguous if pods have different owner kinds, or same owner with different uid
     * @param podList
     * @return whether all pods belong to same owner
     */
    public static boolean checkForPodAmbiguity(List<V1Pod> podList) {
        if (podList == null || podList.size() < 2) {
            return false;
        }
        V1OwnerReference ownerReference = getOwnerReference(podList.get(0));
        if (ownerReference == null) {
            return true;
        }
        String podOwner = ownerReference.getKind();
        String ownerUID = ownerReference.getUid();

        if (StringUtils.isBlank(podOwner) || StringUtils.isBlank(ownerUID)) {
            return true;
        }

        for (V1Pod pod : podList) {
            ownerReference = getOwnerReference(pod);
            if (ownerReference == null) {
                return true;
            }
            if (!podOwner.equals(ownerReference.getKind()) || !ownerUID.equals(ownerReference.getUid())) {
                return true;
            }
        }
        return false;
    }

    private static V1OwnerReference getOwnerReference(V1Pod pod) {
        if (pod == null) {
            return null;
        }
        List<V1OwnerReference> ownerReferences = pod.getMetadata().getOwnerReferences();
        if (ownerReferences == null || ownerReferences.isEmpty()) {
            return null;
        }
        return ownerReferences.get(0);
    }


}
