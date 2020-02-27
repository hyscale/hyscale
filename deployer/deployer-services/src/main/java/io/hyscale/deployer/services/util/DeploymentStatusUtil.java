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

import java.util.ArrayList;
import java.util.List;

import io.hyscale.deployer.services.model.PodCondition;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.utils.HyscaleStringUtil;
import io.hyscale.commons.utils.ResourceLabelUtil;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

/**
 * 
 *	Utility for Service Deployment status
 */
public class DeploymentStatusUtil {

	/**
	 * Status for service not deployed on cluster
	 * @param serviceName
	 * @return DeploymentStatus
	 */
    public static DeploymentStatus getNotDeployedStatus(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            return null;
        }
        DeploymentStatus status = new DeploymentStatus();
        status.setServiceName(serviceName);
        status.setStatus(DeploymentStatus.Status.NOT_DEPLOYED);
        status.setAge(null);
        return status;
    }
    
    public static List<DeploymentStatus> getDeployListNotRunningStatus(List<V1Deployment> deploymentList) {
        if (deploymentList == null) {
            return null;
        }
        List<DeploymentStatus> statuses = new ArrayList<DeploymentStatus>();
        deploymentList.stream().forEach(each -> {
            DeploymentStatus deployStatus = getDeployNotRunningStatus(each);
            if (deployStatus != null) {
                statuses.add(deployStatus);
            }
        });
        return statuses;
    }
    
    public static DeploymentStatus getDeployNotRunningStatus(V1Deployment deployment) {
        if (deployment == null) {
            return null;
        }
        return getNotRunningStatusFromMetadata(deployment.getMetadata());
    }
    
    public static List<DeploymentStatus> getSTSsNotRunningStatus(List<V1StatefulSet> statefulSetList) {
        if (statefulSetList == null) {
            return null;
        }
        List<DeploymentStatus> statuses = new ArrayList<DeploymentStatus>();
        statefulSetList.stream().forEach(each -> {
            DeploymentStatus deployStatus = getSTSNotRunningStatus(each);
            if (deployStatus != null) {
                statuses.add(deployStatus);
            }
        });
        return statuses;
    }
    
    public static DeploymentStatus getSTSNotRunningStatus(V1StatefulSet statefulSet) {
        if (statefulSet == null) {
            return null;
        }
        return getNotRunningStatusFromMetadata(statefulSet.getMetadata());
    }
    
    private static DeploymentStatus getNotRunningStatusFromMetadata(V1ObjectMeta metadata) {
        if (metadata == null) {
            return null;
        }
        DeploymentStatus status = new DeploymentStatus();
        String serviceName = ResourceLabelUtil.getServiceName(metadata.getLabels());
        status.setServiceName(serviceName);
        status.setStatus(DeploymentStatus.Status.NOT_RUNNING);
        status.setAge(metadata.getCreationTimestamp());
        return status;
    }
    
    /**
     * Message from pods not in ready state
     * @param v1PodList
     * @return null if pods are in ready condition, else pods message
     */
    public static String getMessage(List<V1Pod> v1PodList) {
	if (v1PodList == null || v1PodList.isEmpty()) {
	    return null;
	}
        boolean ready = true;
        StringBuilder message = new StringBuilder();
        for (V1Pod v1Pod : v1PodList) {

            ready = ready && K8sPodUtil.checkForPodCondition(v1Pod, PodCondition.READY);
            if (!ready) {
                message.append(K8sPodUtil.getAggregatedStatusOfContainersForPod(v1Pod));
                String failureMessage = K8sPodUtil.getPodMessage(v1Pod);
                if(StringUtils.isBlank(failureMessage)){
                    failureMessage = K8sPodUtil.getFailureMessage(v1Pod);
                }
                if (StringUtils.isNotBlank(failureMessage)) {
                    message.append("::");
                    message.append(failureMessage);
                    message.append(ToolConstants.COMMA);
                }
            }
        }
        if (!ready) {
            return HyscaleStringUtil.removeSuffixStr(message, ToolConstants.COMMA);
        }
        return null;
    }

    public static DeploymentStatus.Status getStatus(List<V1Pod> v1PodList) {
        if (v1PodList == null || v1PodList.isEmpty()) {
            return DeploymentStatus.Status.NOT_DEPLOYED;
        }
        boolean ready = true;
        for (V1Pod v1Pod : v1PodList) {
            ready = K8sPodUtil.checkForPodCondition(v1Pod, PodCondition.READY) && ready;
        }
        if (ready) {
            return DeploymentStatus.Status.RUNNING;
        } else {
            return DeploymentStatus.Status.NOT_RUNNING;
        }
    }

    /**
     * DateTime from 1st pod
     * Give estimation of how long ago current status was updated
     * @param v1PodList
     * @return Datetime
     */
    public static DateTime getAge(List<V1Pod> v1PodList) {
        if (v1PodList == null || v1PodList.isEmpty()) {
            return null;
        }
        DateTime dateTime = null;
        V1Pod v1Pod = v1PodList.get(0);
        dateTime = v1Pod.getStatus().getStartTime();
        return dateTime != null ? dateTime : v1Pod.getMetadata().getCreationTimestamp();
    }

}
