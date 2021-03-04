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
import io.kubernetes.client.openapi.models.V1Event;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class ImagePullBackOffAction extends ActionNode<TroubleshootingContext> {

    private static final String FAILED = "Failed";
    private static final String IMAGE_NOT_FOUND = "manifest for .* not found";
    private static final String INVALID_CREDENTIALS = ".* unauthorized: incorrect username or password";
    private static final String AUTHORIZATION_REQUIRED = ".* unauthorized: authentication required";
    private static final Pattern imageNotFoundPattern = Pattern.compile(IMAGE_NOT_FOUND);
    private static final Pattern invalidCredentialsPattern = Pattern.compile(INVALID_CREDENTIALS);
    private static final Pattern authorisationRequiredPattern = Pattern.compile(AUTHORIZATION_REQUIRED);

    @Override
    public void process(TroubleshootingContext context) {

        Object obj = context.getAttribute(FailedResourceKey.FAILED_POD_EVENTS);
        if (obj != null) {
            List<V1Event> eventList = (List<V1Event>) FailedResourceKey.FAILED_POD_EVENTS.getKlazz().cast(obj);
            if (eventList != null && !eventList.isEmpty()) {
                boolean actedOn = false;
                for (V1Event event : eventList) {
                    if (!FAILED.equals(event.getReason())) {
                        continue;
                    }
                    DiagnosisReport report = new DiagnosisReport();
                    if (imageNotFoundPattern.matcher(event.getMessage()).find()) {
                        report.setReason(AbstractedErrorMessage.FIX_IMAGE_NAME.formatReason(context.getServiceMetadata().getServiceName()));
                        report.setRecommendedFix(AbstractedErrorMessage.FIX_IMAGE_NAME.formatMessage(context.getServiceMetadata().getServiceName()));
                        context.addReport(report);
                        actedOn = true;
                        break;
                    } else if (invalidCredentialsPattern.matcher(event.getMessage()).find() || authorisationRequiredPattern.matcher(event.getMessage()).find()) {
                        report.setReason(AbstractedErrorMessage.INVALID_PULL_REGISTRY_CREDENTIALS.getMessage());
                        report.setRecommendedFix(AbstractedErrorMessage.INVALID_PULL_REGISTRY_CREDENTIALS.formatMessage(SetupConfig.getMountOfDockerConf(SetupConfig.USER_HOME_DIR + "/.docker/config")));
                        context.addReport(report);
                        actedOn = true;
                        break;
                    }
                }
                if (!actedOn) {
                    context.addReport(getDefaultReport());
                }
            }
        } else {
            context.addReport(getDefaultReport());
        }
    }

    @Override
    public String describe() {
        return "Please check your image name & tag & target registry credentials";
    }

    private DiagnosisReport getDefaultReport() {
        DiagnosisReport report = new DiagnosisReport();
        report.setRecommendedFix(AbstractedErrorMessage.IMAGEPULL_BACKOFF_ACTION.formatMessage(SetupConfig.getMountOfDockerConf(SetupConfig.USER_HOME_DIR + "/.docker/config")));
        report.setReason(AbstractedErrorMessage.IMAGEPULL_BACKOFF_ACTION.getReason());
        return report;
    }

}
