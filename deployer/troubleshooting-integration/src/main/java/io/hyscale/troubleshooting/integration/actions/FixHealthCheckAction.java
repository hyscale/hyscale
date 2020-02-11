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
import io.hyscale.troubleshooting.integration.models.*;
import io.kubernetes.client.models.V1Event;
import org.springframework.stereotype.Component;

@Component
public class FixHealthCheckAction extends ActionNode<TroubleshootingContext> {

    @Override
    public void process(TroubleshootingContext context) {

        Object obj = context.getAttribute(FailedResourceKey.UNHEALTHY_POD_EVENT);
        String eventMessage = null;
        if (obj != null) {
            V1Event event = (V1Event) FailedResourceKey.UNHEALTHY_POD_EVENT.getKlazz().cast(obj);
            eventMessage = event != null ? event.getMessage() : null;
        }

        DiagnosisReport report = new DiagnosisReport();
        report.setReason(AbstractedErrorMessage.LIVENESS_PROBE_FAILURE.formatReason(context.getServiceInfo().getServiceName(),eventMessage));
        report.setRecommendedFix(AbstractedErrorMessage.LIVENESS_PROBE_FAILURE.getMessage());
        context.addReport(report);
    }

    @Override
    public String describe() {
        return "Fix Liveness probe ";
    }

}
