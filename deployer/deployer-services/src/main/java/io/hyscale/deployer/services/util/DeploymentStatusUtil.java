package io.hyscale.deployer.services.util;

import java.util.List;

import io.hyscale.deployer.services.model.PodCondition;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.utils.HyscaleStringUtil;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.kubernetes.client.models.V1Pod;

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
        DeploymentStatus status = new DeploymentStatus();
        status.setServiceName(serviceName);
        status.setStatus(DeploymentStatus.Status.NOT_DEPLOYED);
        status.setAge(null);
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
        dateTime = v1PodList.get(0).getStatus().getStartTime();
        return dateTime;
    }

}
