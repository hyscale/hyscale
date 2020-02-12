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
import io.hyscale.troubleshooting.integration.conditions.IsPodsReadinessFailing;
import io.hyscale.troubleshooting.integration.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FixImageNameAction extends ActionNode<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(FixImageNameAction.class);

    @Override
    public void process(TroubleshootingContext context) {
        DiagnosisReport report = new DiagnosisReport();
        report.setRecommendedFix(AbstractedErrorMessage.FIX_IMAGE_NAME.getMessage());
        report.setReason(AbstractedErrorMessage.FIX_IMAGE_NAME.getReason());
        context.addReport(report);
    }

    @Override
    public String describe() {
        return "Fix image name";
    }

}
