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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.troubleshooting.integration.models.AbstractedErrorMessage;
import io.hyscale.troubleshooting.integration.models.ActionNode;
import io.hyscale.troubleshooting.integration.models.DiagnosisReport;
import io.hyscale.troubleshooting.integration.models.FailedResourceKey;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.kubernetes.client.openapi.models.V1Event;
import io.kubernetes.client.openapi.models.V1ObjectReference;

/**
 * Action node handles case when pods are not available but pod owner is available
 * @author tushar
 *
 */
@Component
public class ParentFailureAction extends ActionNode<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(ParentFailureAction.class);

    private static final String INVALID_VOLUME_NAME = "metadata\\.name: Invalid value";

    private static final String INVALID_VOLUME_NAME_LENGTH = "spec\\.volumes\\[\\d\\]\\.name";
    
    private static final String MULTIPLE_DEFAULT_STORAGE_CLASS = "default StorageClasses were found";
    
    private static final String INVALID_RESOURCE_NAME = "metadata\\.labels: Invalid value";

    private static final List<Pattern> invalidVolumeNamePattern = Arrays
            .asList(Pattern.compile(INVALID_VOLUME_NAME_LENGTH), Pattern.compile(INVALID_VOLUME_NAME));
    
    private static final Pattern multipleDefaultStorageClassPattern = Pattern.compile(MULTIPLE_DEFAULT_STORAGE_CLASS);

    private static final Pattern invalidResourceNamePattern = Pattern.compile(INVALID_RESOURCE_NAME);
    
    @Override
    public void process(TroubleshootingContext context) {
        DiagnosisReport report = new DiagnosisReport();
        Object eventObj = context.getAttribute(FailedResourceKey.FAILED_PARENT_EVENT);
        if (eventObj == null) {
            logger.debug("No failure event found to process");
            return;
        }
        V1Event event = (V1Event) eventObj;
        if (invalidVolumeNamePattern.stream().anyMatch(pattern -> pattern.matcher(event.getMessage()).find())) {
            report.setReason(AbstractedErrorMessage.INVALID_VOLUME_NAME.getReason());
            report.setRecommendedFix(AbstractedErrorMessage.INVALID_VOLUME_NAME.getMessage());
            context.addReport(report);
            return;
        }
        if (multipleDefaultStorageClassPattern.matcher(event.getMessage()).find()) {
            report.setReason(AbstractedErrorMessage.MULTIPLE_DEFAULT_STORAGE_CLASS.getReason());
            report.setRecommendedFix(AbstractedErrorMessage.MULTIPLE_DEFAULT_STORAGE_CLASS.getMessage());
            context.addReport(report);
            return;
        }
        if (invalidResourceNamePattern.matcher(event.getMessage()).find()) {
            V1ObjectReference eventRefObj = event.getInvolvedObject();
            String resourceDetails = eventRefObj.getKind() + " " + eventRefObj.getName();
            report.setReason(AbstractedErrorMessage.INVALID_RESOURCE_NAME.formatReason(resourceDetails));
            report.setRecommendedFix(AbstractedErrorMessage.INVALID_RESOURCE_NAME.getMessage());
            context.addReport(report);
        }
    }

    @Override
    public String describe() {
        return "Kubernetes controller failed to create pods";
    }

}
