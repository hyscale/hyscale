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

import io.hyscale.troubleshooting.integration.models.AbstractedErrorMessage;
import io.hyscale.troubleshooting.integration.models.ActionNode;
import io.hyscale.troubleshooting.integration.models.DiagnosisReport;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import org.springframework.stereotype.Component;

/*
 * Prepares a diagnosis report stating that service deployed to cluster has no replicas
 */
@Component
public class ServiceWithZeroReplicasAction extends ActionNode<TroubleshootingContext> {

    @Override
    public void process(TroubleshootingContext context) {
        DiagnosisReport report = new DiagnosisReport();
        report.setReason(AbstractedErrorMessage.SERVICE_WITH_ZERO_REPLICAS.getReason());
        report.setRecommendedFix(AbstractedErrorMessage.SERVICE_WITH_ZERO_REPLICAS.getMessage());
        context.addReport(report);
    }

    @Override
    public String describe() {
        return "Service deployed with zero replicas";
    }
}
