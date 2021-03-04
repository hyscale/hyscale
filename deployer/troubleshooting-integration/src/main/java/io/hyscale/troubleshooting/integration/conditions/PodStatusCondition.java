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
package io.hyscale.troubleshooting.integration.conditions;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.HyscaleContextUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.model.PodStatusUtil;
import io.hyscale.troubleshooting.integration.actions.*;
import io.hyscale.troubleshooting.integration.models.FailedResourceKey;
import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.deployer.services.model.PodStatus;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.kubernetes.client.openapi.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * {@link PodStatusCondition}  checks whether a kubernetes pod
 * has a status in @{@link PodStatus} . The condition is
 * checked based on the pod status from the {@link TroubleshootingContext}.
 * The status of all replicas is determined and aggregated when the first
 * failed state of any replica is encountered. Troubleshooting is
 * performed on this podstatus that is derived.
 */

@Component
public class PodStatusCondition implements Node<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(PodStatusCondition.class);

    @Autowired
    private ServiceNotDeployedAction serviceNotDeployedAction;
    
    @Autowired
    private ParentStatusCondition parentStatusCondition;

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        if (context.getResourceInfos() == null) {
            return serviceNotDeployedAction;
        }

        List<TroubleshootingContext.ResourceInfo> resourceInfos = context.getResourceInfos().getOrDefault(ResourceKind.POD.getKind(), null);
        if (resourceInfos == null || resourceInfos.isEmpty()) {
            if (context.isTrace()) {
                logger.debug("Cannot find any pods for the service {}", context.getServiceMetadata().getServiceName());
            }
            return parentStatusCondition;
        }
        PodStatus effectivePodStatus = PodStatus.DEFAULT;
        
        for (TroubleshootingContext.ResourceInfo each : resourceInfos) {
            if (each == null || each.getResource() == null) {
                continue;
            }
            if (each.getResource() instanceof V1Pod) {
                V1Pod v1Pod = (V1Pod) each.getResource();
                String aggregatedStatus = PodStatusUtil.currentStatusOf(v1Pod);
                if (context.isTrace()) {
                    logger.debug("Aggregated status of pod {} of service {}", v1Pod.getMetadata().getName(),
                            context.getServiceMetadata().getServiceName());
                }
                if (StringUtils.isEmpty(aggregatedStatus)) {
                    continue;
                }
                effectivePodStatus = PodStatus.get(aggregatedStatus);
                /*
                   First encountered  Pod that is failed
                 */
                if (effectivePodStatus.isFailed()) {
                    context.addAttribute(FailedResourceKey.OBSERVED_POD_STATUS, aggregatedStatus);
                    if (context.isTrace()) {
                        logger.debug("Observed failed pod {} and status {}", v1Pod.getMetadata().getName(), effectivePodStatus.getStatus());
                    }
                    context.addAttribute(FailedResourceKey.FAILED_POD, v1Pod);
                    context.addAttribute(FailedResourceKey.FAILED_POD_EVENTS, each.getEvents());
                    break;
                }
            }
        }
        return HyscaleContextUtil.getSpringBean(getNextNode(effectivePodStatus));
    }

    @Override
    public String describe() {
        return "Checks for pod status and continue the workflow based on the status";
    }

    public Class<? extends Node> getNextNode(PodStatus podStatus) {
        Class<? extends Node> defaultActionClass = DefaultAction.class;
        if (podStatus == null) {
            return defaultActionClass;
        }
        switch (podStatus) {
            case IMAGEPULL_BACKOFF:
            case ERR_IMAGE_PULL:
                return ImagePullBackOffAction.class;
            case CRASHLOOP_BACKOFF:
                return MissingCMDorStartCommandsCondition.class;
            case RUNNING:
                return ArePodsReady.class;
            case OOMKILLED:
                return FixCrashingApplication.class;
            case PENDING:
                return IsClusterFull.class;
            case ERROR:
                return ArePodsReady.class;
            case COMPLETED:
                return FixCrashingApplication.class;
            case TERMINATING:
                return ParentStatusCondition.class;
            case RUN_CONTAINER_ERROR:
            case DEFAULT:
                return defaultActionClass;
            default:
                return defaultActionClass;
                
        }
    }
}
