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

import java.util.List;

import io.kubernetes.client.openapi.models.V1ContainerState;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1Pod;

public class PodStatusUtil {
    
    private PodStatusUtil() {}

    public static String currentStatusOf(V1Pod v1Pod) {
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
        String containerAggStatus = validateAndGetContainerStatuses(v1Pod.getStatus().getContainerStatuses(), false);
        return containerAggStatus != null ? containerAggStatus : v1Pod.getStatus().getPhase();
    }

    public static String lastStateOf(V1Pod v1Pod) {
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
        String containerAggStatus = validateAndGetContainerStatuses(v1Pod.getStatus().getContainerStatuses(), true);
        return containerAggStatus != null ? containerAggStatus : v1Pod.getStatus().getPhase();
    }

    private static String validateAndGetContainerStatuses(List<V1ContainerStatus> containerStatuses, boolean withLastState) {
        if (containerStatuses == null || containerStatuses.isEmpty()) {
            return null;
        }
        String aggregateStatus = null;
        for (V1ContainerStatus each : containerStatuses) {
            if (withLastState && !each.getReady() && each.getLastState() != null) {
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

    /**
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
                // do nothing
            } else if (each.getState().getWaiting() != null) {
                initContainerStatus = each.getState().getWaiting().getReason();
                break;
            } else if (each.getState().getRunning() != null) {
                // TODO Handle
            }
        }
        return initContainerStatus;
    }
    
	public static V1ContainerState getLastState(V1Pod pod) {
		List<V1ContainerStatus> v1ContainerStatus = pod.getStatus().getContainerStatuses();
		for (V1ContainerStatus containerStatus : v1ContainerStatus) {
			if (!containerStatus.getReady()) {
				return containerStatus.getLastState();
			}
		}
		return null;
	}

	public static Integer getExitCode(V1ContainerState v1ContainerState) {
		if (v1ContainerState!=null && v1ContainerState.getTerminated() != null) {
			return v1ContainerState.getTerminated().getExitCode();
		}
		return null;
	}

}
