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
package io.hyscale.troubleshooting.integration.actions;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.deployer.services.model.PodStatus;
import io.hyscale.deployer.services.model.PodStatusUtil;
import io.hyscale.troubleshooting.integration.conditions.IsPodsReadinessFailing;
import io.hyscale.troubleshooting.integration.models.*;
import io.kubernetes.client.models.V1Event;
import io.kubernetes.client.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FixCrashingApplication extends ActionNode<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(FixCrashingApplication.class);

    @Override
    public void process(TroubleshootingContext context) {
        Object obj = context.getAttribute(FailedResourceKey.FAILED_POD);
        String lastState = null;
        if (obj != null) {
            V1Pod pod = (V1Pod) FailedResourceKey.FAILED_POD.getKlazz().cast(obj);
            lastState = pod != null ? PodStatusUtil.lastStateOf(pod) : null;
        }

        DiagnosisReport report = new DiagnosisReport();
        if (lastState != null) {
            if (lastState.equals(PodStatus.OOMKILLED.getStatus())) {
                report.setReason(AbstractedErrorMessage.NOT_ENOUGH_MEMORY_FOUND.formatReason(context.getServiceInfo().getServiceName()));
                report.setRecommendedFix(AbstractedErrorMessage.NOT_ENOUGH_MEMORY_FOUND.getMessage());
            } else if (lastState.equals(PodStatus.COMPLETED.getStatus())) {
                report.setReason(AbstractedErrorMessage.INVALID_STARTCOMMANDS_FOUND.getReason());
                report.setRecommendedFix(AbstractedErrorMessage.INVALID_STARTCOMMANDS_FOUND.getMessage());
            } else {
                report.setReason(AbstractedErrorMessage.APPLICATION_CRASH.getReason());
                report.setRecommendedFix(AbstractedErrorMessage.APPLICATION_CRASH.getMessage());
            }
        } else {
            report.setReason(AbstractedErrorMessage.APPLICATION_CRASH.getReason());
            report.setRecommendedFix(AbstractedErrorMessage.APPLICATION_CRASH.getMessage());
        }
        context.addReport(report);
    }

    @Override
    public String describe() {
        return "Fix your crashing application";
    }

}
