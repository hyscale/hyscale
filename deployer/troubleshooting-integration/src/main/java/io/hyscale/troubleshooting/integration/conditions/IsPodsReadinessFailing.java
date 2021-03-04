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
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.model.PodStatus;
import io.hyscale.deployer.services.util.K8sPodUtil;
import io.hyscale.troubleshooting.integration.actions.FixCrashingApplication;
import io.hyscale.troubleshooting.integration.actions.TryAfterSometimeAction;
import io.hyscale.troubleshooting.integration.errors.TroubleshootErrorCodes;
import io.hyscale.troubleshooting.integration.models.*;
import io.hyscale.troubleshooting.integration.actions.FixHealthCheckAction;
import io.kubernetes.client.openapi.models.V1Event;
import io.kubernetes.client.openapi.models.V1Pod;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class IsPodsReadinessFailing implements Node<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(IsPodsReadinessFailing.class);

    private static final String UNHEALTHY_REASON = "Unhealthy";

    @Autowired
    private FixHealthCheckAction fixHealthCheckAction;

    @Autowired
    private TryAfterSometimeAction tryAfterSometimeAction;

    @Autowired
    private FixCrashingApplication fixCrashingApplication;

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        List<TroubleshootingContext.ResourceInfo> resourceInfos = context.getResourceInfos().get(ResourceKind.POD.getKind());
        DiagnosisReport report = new DiagnosisReport();
        if (resourceInfos == null || resourceInfos.isEmpty()) {
            report.setReason(AbstractedErrorMessage.SERVICE_NOT_DEPLOYED.formatReason(context.getServiceMetadata().getServiceName()));
            report.setRecommendedFix(AbstractedErrorMessage.SERVICE_NOT_DEPLOYED.getMessage());
            context.addReport(report);
            throw new HyscaleException(TroubleshootErrorCodes.SERVICE_IS_NOT_DEPLOYED, context.getServiceMetadata().getServiceName());
        }

        List<V1Event> v1Events = new ArrayList<>();
        Object obj = context.getAttribute(FailedResourceKey.UNREADY_POD);
        V1Pod unhealthyPod = null;
        if (obj != null) {
            unhealthyPod = (V1Pod) FailedResourceKey.UNREADY_POD.getKlazz().cast(obj);
        }

        Object restartsObj = context.getAttribute(FailedResourceKey.RESTARTS);
        boolean restartsObserved = false;
        if (restartsObj != null) {
            restartsObserved = BooleanUtils.toBoolean((Boolean)FailedResourceKey.RESTARTS.getKlazz().cast(restartsObj));
        }
        // Get all the events of the unhealthy pod from previous conditionNode or fetch it from the existing
        // set of pods
        for (TroubleshootingContext.ResourceInfo resourceInfo : resourceInfos) {
            if (resourceInfo != null && resourceInfo.getResource() instanceof V1Pod) {
                V1Pod pod = (V1Pod) resourceInfo.getResource();
                if (unhealthyPod == null) {
                    PodStatus status = PodStatus.get(K8sPodUtil.getAggregatedStatusOfContainersForPod(pod));
                    if (status != null && !status.isFailed()) {
                        v1Events.addAll(resourceInfo.getEvents());
                    }
                } else {
                    if (unhealthyPod.getMetadata().getName().equals(pod.getMetadata().getName())) {
                        v1Events.addAll(resourceInfo.getEvents());
                    }
                }
            }
        }

        if (v1Events.isEmpty()) {
            logger.debug("No events found when checking for pod readiness");
            report.setReason(AbstractedErrorMessage.CANNOT_FIND_EVENTS.getReason());
            report.setRecommendedFix(AbstractedErrorMessage.CANNOT_FIND_EVENTS.getMessage());
            context.addReport(report);
            return fixHealthCheckAction;
        }
        boolean unhealthy = v1Events.stream().
                anyMatch(each -> {
                    if (UNHEALTHY_REASON.equals(each.getReason())) {
                        context.addAttribute(FailedResourceKey.UNHEALTHY_POD_EVENT, each);
                        return true;
                    }
                    return false;
                });
        if (unhealthy) {
            return fixHealthCheckAction;
        } else {
            return restartsObserved ? fixCrashingApplication : tryAfterSometimeAction;
        }
    }

    @Override
    public String describe() {
        return "Readiness failing ?";
    }

}
