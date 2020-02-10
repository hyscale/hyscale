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

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.troubleshooting.integration.models.*;

public class ImagePullBackOffAction extends ActionNode<TroubleshootingContext> {

    @Override
    public void process(TroubleshootingContext context) {
        DiagnosisReport report = new DiagnosisReport();
        report.setRecommendedFix(AbstractedErrorMessage.IMAGEPULL_BACKOFF_ACTION.getMessage());
        report.setReason(AbstractedErrorMessage.IMAGEPULL_BACKOFF_ACTION.formatMessage(SetupConfig.getMountOfDockerConf(SetupConfig.USER_HOME_DIR + "/.docker/config")));
        context.addReport(report);
    }

    @Override
    public String describe() {
        return "Please check your image name & tag & target registry credentials";
    }

}
