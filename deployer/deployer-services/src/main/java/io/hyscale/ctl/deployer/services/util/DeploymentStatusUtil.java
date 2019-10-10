package io.hyscale.ctl.deployer.services.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import io.hyscale.ctl.commons.constants.ToolConstants;
import io.hyscale.ctl.commons.utils.HyscaleStringUtil;
import io.hyscale.ctl.deployer.core.model.DeploymentStatus;
import io.hyscale.ctl.deployer.services.model.PodCondition;
import io.kubernetes.client.models.V1Pod;

public class DeploymentStatusUtil {

    public static DeploymentStatus getNotDeployedStatus(String serviceName) {
        DeploymentStatus status = new DeploymentStatus();
        status.setServiceName(serviceName);
        status.setStatus(DeploymentStatus.Status.NOT_DEPLOYED);
        status.setDateTime(null);
        return status;
    }

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

    public static DateTime getAge(List<V1Pod> v1PodList) {
        if (v1PodList == null || v1PodList.isEmpty()) {
            return null;
        }
        DateTime dateTime = null;
        dateTime = v1PodList.get(0).getStatus().getStartTime();
        return dateTime;
    }

}
