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
import org.springframework.stereotype.Component;

@Component
public class DefaultAction extends ActionNode<TroubleshootingContext> {

    private static final String KUBERNETES_ERROR_MESSAGE = "Kubernetes error message follows: \\n %s";

    @Override
    public void process(TroubleshootingContext context) {
        Object obj = context.getAttribute(FailedResourceKey.OBSERVED_POD_STATUS);
        String errorMessage = null;
        if (obj != null && obj instanceof String) {
            errorMessage = String.format(KUBERNETES_ERROR_MESSAGE, (String) FailedResourceKey.OBSERVED_POD_STATUS.getKlazz().cast(obj));
        }

        DiagnosisReport report = new DiagnosisReport();
        report.setReason(AbstractedErrorMessage.CANNOT_INFER_ERROR.getReason());
        report.setRecommendedFix(AbstractedErrorMessage.CANNOT_INFER_ERROR.formatMessage(errorMessage));
        context.addReport(report);
    }

    @Override
    public String describe() {
        return "Default Action when error cannot be inferred from the service state";
    }

}
